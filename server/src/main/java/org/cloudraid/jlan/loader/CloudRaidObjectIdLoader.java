package org.cloudraid.jlan.loader;

import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.filesys.db.DBException;
import org.alfresco.jlan.server.filesys.db.ObjectIdFileLoader;
import org.alfresco.jlan.server.filesys.loader.FileLoaderException;
import org.alfresco.jlan.server.filesys.loader.FileSegment;
import org.alfresco.jlan.util.NameValueList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cloudraid.ida.persistence.api.*;
import org.cloudraid.ida.persistence.crypto.EncryptionKeyRepository;
import org.cloudraid.ida.persistence.crypto.EncryptionProvider;
import org.cloudraid.ida.persistence.crypto.jce.JceEncryptionProvider;
import org.cloudraid.ida.persistence.impl.CrsInformationDispersalAlgorithm;
import org.cloudraid.ida.persistence.impl.FilesystemFragmentRepository;
import org.cloudraid.ida.persistence.impl.InformationDispersalPersistenceServiceImpl;
import org.cloudraid.jlan.util.JlanConfiguration;
import org.springframework.extensions.config.ConfigElement;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implementation of {@link ObjectIdFileLoader} that uses the IDA Persistence Engine for storing/loading files.
 */
public class CloudRaidObjectIdLoader extends ObjectIdFileLoader {

    private static final Logger logger = Logger.getLogger(CloudRaidObjectIdLoader.class);

    protected InformationDispersalPersistenceService persistenceService;
    protected Executor threadPoolExecutor;

    @Override
    public void initializeLoader(ConfigElement params, DeviceContext ctx) throws FileLoaderException, IOException {
        super.initializeLoader(params, ctx);

        ConfigElement informationDispersalConfig = params.getChild("InformationDispersal");
        if (informationDispersalConfig == null) {
            throw new FileLoaderException("InformationDispersal not specified");
        }

        if (!(getContext().getDBInterface() instanceof FragmentMetaDataRepository)) {
            throw new FileLoaderException("DBInterface should implement " + FragmentMetaDataRepository.class.getName());
        }
        if (!(getContext().getDBInterface() instanceof EncryptionKeyRepository)) {
            throw new FileLoaderException("DBInterface should implement " + EncryptionKeyRepository.class.getName());
        }

        Context context = new Context();

        context.setFragmentMetaDataRepository((FragmentMetaDataRepository) getContext().getDBInterface());
        context.setEncryptionKeyRepository((EncryptionKeyRepository) getContext().getDBInterface());

        setUpThreadPoolExecutor(context);
        setUpFragmentRepositories(informationDispersalConfig, context);
        setUpIda(informationDispersalConfig, context);
        setUpEncryptionProvider(informationDispersalConfig, context);
        setUpInformationDispersalPersistenceService(informationDispersalConfig, context);
    }

    @Override
    public void loadFileData(int fileId, int streamId, String objectId, FileSegment fileSeg) throws IOException {
        File dataFile = new File(fileSeg.getTemporaryFile());
        byte[] data;

        if (logger.isDebugEnabled()) {
            String fileInfoStr = getFileInfoString(fileId, streamId, objectId);
            logger.debug("Loading data of " + fileInfoStr);
        }

        try {
            data = persistenceService.loadData(objectId);
        } catch (Exception e) {
            String fileInfoStr = getFileInfoString(fileId, streamId, objectId);
            logger.error("Error while loading data of " + fileInfoStr, e);

            throw new IOException("Error while loading data of " + fileInfoStr, e);
        }

        if (logger.isDebugEnabled()) {
            String fileInfoStr = getFileInfoString(fileId, streamId, objectId);

            logger.debug("Finished loading data of " + fileInfoStr);
            logger.debug("Writing data of " + fileInfoStr + " to temp file " + dataFile);
        }

        try {
            FileUtils.writeByteArrayToFile(dataFile, data);
        } catch (IOException e) {
            String fileInfoStr = getFileInfoString(fileId, streamId, objectId);
            logger.error("Unable to write data of " + fileInfoStr + " to temp file " + dataFile, e);

            throw new IOException("Unable to write data of " + fileInfoStr + " to temp file " + dataFile, e);
        }

        fileSeg.setReadableLength(data.length);
        fileSeg.signalDataAvailable();
    }

    @Override
    public String saveFileData(int fileId, int streamId, FileSegment fileSeg, NameValueList attrs) throws IOException {
        File dataFile = new File(fileSeg.getTemporaryFile());
        byte[] data;

        if (logger.isDebugEnabled()) {
            String fileInfoStr = getFileInfoString(fileId, streamId, null);
            logger.debug("Reading data of " + fileInfoStr + " from temp file " + dataFile);
        }

        try {
            data = FileUtils.readFileToByteArray(dataFile);
        } catch (Exception e) {
            String fileInfoStr = getFileInfoString(fileId, streamId, null);
            logger.error("Unable to read data of " + fileInfoStr + " from temp file " + dataFile, e);

            throw new IOException("Unable to read data of " + fileInfoStr + " from temp file " + dataFile, e);
        }

        String dataId = createDataId(fileId, streamId, data);

        if (logger.isDebugEnabled()) {
            String fileInfoStr = getFileInfoString(fileId, streamId, dataId);
            logger.debug("Saving data of " + fileInfoStr);
        }

        try {
            persistenceService.saveData(dataId, data);
        } catch (Exception e) {
            String fileInfoStr = getFileInfoString(fileId, streamId, dataId);
            logger.error("Error while saving data of " + fileInfoStr, e);

            throw new IOException("Error while saving data of " + fileInfoStr, e);
        }

        if (logger.isDebugEnabled()) {
            String fileInfoStr = getFileInfoString(fileId, streamId, dataId);
            logger.debug("Finished saving data of " + fileInfoStr);
        }

        // Delete any previous data of the file/stream, so that it won't hang a long time in the repositories and thus
        // reduce disk usage.
        deleteOldSavedDataOnBackground(fileId, streamId);

        return dataId;
    }

    /**
     * Sets up the thread pool executor.
     */
    protected void setUpThreadPoolExecutor(Context context) {
        threadPoolExecutor = Executors.newCachedThreadPool();

        context.setThreadPoolExecutor(threadPoolExecutor);
    }

    /**
     * Sets up the fragment repositories for the InformationDispersalPersistenceService, given the specified config.
     *
     * @param informationDispersalConfig
     *          the &lt;InformationDispersal&gt; config node, which should contain a &lt;FragmentRepositories&gt; config
     *          child node.
     * @param context
     *          the IDA Persistence Engine component context.
     * @throws FileLoaderException
     */
    protected void setUpFragmentRepositories(ConfigElement informationDispersalConfig, Context context) throws FileLoaderException {
        List<FragmentRepository> repositories = new ArrayList<FragmentRepository>();

        ConfigElement reposConfig = informationDispersalConfig.getChild("FragmentRepositories");
        if (reposConfig == null) {
            throw new FileLoaderException("FragmentRepositories not specified");
        }

        Map<String, Class<?>> repositoryTypes = createDefaultRepositoryTypes();

        List<ConfigElement> reposConfigChildren = reposConfig.getChildren();
        for (ConfigElement reposConfigChild : reposConfigChildren) {
            if (reposConfigChild.getName().equals("RepositoryType")) {
                ConfigElement classConfig = reposConfigChild.getChild("Class");
                ConfigElement typeConfig = reposConfigChild.getChild("Type");

                if (classConfig == null || StringUtils.isEmpty(classConfig.getValue())) {
                    throw new FileLoaderException("Class for RepositoryType not specified or null");
                }
                if (typeConfig == null || StringUtils.isEmpty(typeConfig.getValue())) {
                    throw new FileLoaderException("Type for RepositoryType not specified or null");
                }
                
                ClassLoader classLoader = getClass().getClassLoader();

                try {
                    repositoryTypes.put(typeConfig.getValue(), classLoader.loadClass(classConfig.getValue()));
                } catch (ClassNotFoundException e) {
                    throw new FileLoaderException("Failed to load class " + classConfig.getValue());
                }
            } else if (reposConfigChild.getName().equals("Repository")) {
                String type = reposConfigChild.getAttribute("type");
                if (StringUtils.isEmpty(type)) {
                    throw new FileLoaderException("Repository not specified or null");
                }

                Class<?> repoClass = repositoryTypes.get(type);
                if (repoClass == null) {
                    throw new FileLoaderException("Unrecognized repository type '" + type + "'. Make sure the " +
                            "corresponding RepositoryType is defined before the Repository");
                }

                FragmentRepository repo;
                try {
                    repo = (FragmentRepository) repoClass.newInstance();
                } catch (Exception e) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Failed to instantiate " + repoClass.getName() +
                            ": " + e.getMessage());
                }

                try {
                    repo.init(new JlanConfiguration(reposConfigChild, context));
                } catch (Exception e) {
                    throw new FileLoaderException("Failed to initialize " + repoClass.getName() + ": " +  e.getMessage());
                }

                repositories.add(repo);
            } else {
                throw new FileLoaderException("Unrecognized element inside FragmentRepositories: " + reposConfigChild
                        .getName());
            }
        }

        if (repositories.isEmpty()) {
            throw new FileLoaderException("No repositories specified");
        }

        context.setFragmentRepositories(repositories);
    }

    /**
     * Creates and returns the map with the default repository types.
     */
    protected Map<String, Class<?>> createDefaultRepositoryTypes() {
        Map<String, Class<?>> repositoryTypes = new HashMap<String, Class<?>>();
        repositoryTypes.put("file", FilesystemFragmentRepository.class);

        return repositoryTypes;
    }

    /**
     * Sets up the IDA for the InformationDispersalPersistenceService, given the specified config.
     *
     * @param informationDispersalConfig
     *          the &lt;InformationDispersal&gt; config node, which should contain a &lt;InformationDispersalAlgorithm&gt;
     *          config child node.
     * @param context
     *          the IDA Persistence Engine component context.
     * @throws FileLoaderException
     */
    protected void setUpIda(ConfigElement informationDispersalConfig, Context context) throws FileLoaderException {
        InformationDispersalAlgorithm ida;

        ConfigElement idaConfig = informationDispersalConfig.getChild("InformationDispersalAlgorithm");
        if (idaConfig != null) {
            ConfigElement classConfig = idaConfig.getChild("Class");
            if (classConfig != null && StringUtils.isNotEmpty(classConfig.getValue())) {
                ClassLoader classLoader = getClass().getClassLoader();
                try {
                    ida = (InformationDispersalAlgorithm) classLoader.loadClass(classConfig.getValue()).newInstance();
                } catch (ClassNotFoundException e) {
                    throw new FileLoaderException("Failed to load class " + classConfig.getValue());
                } catch (Exception e) {
                    throw new FileLoaderException("Failed to instantiate " + classConfig.getValue() + ": " + e.getMessage());
                }
            } else {
                ida = new CrsInformationDispersalAlgorithm();
            }
        } else {
            throw new FileLoaderException("InformationDispersalAlgorithm not specified");
        }

        try {
            ida.init(new JlanConfiguration(idaConfig, context));
        } catch (Exception e) {
            throw new FileLoaderException("Failed to initialize " + ida.getClass().getName() + ": " + e.getMessage());
        }

        context.setInformationDispersalAlgorithm(ida);
    }

    /**
     * Sets up the EncryptionProvider for the InformationDispersalPersistenceService, given the specified config (required only when
     * using {@link org.cloudraid.ida.persistence.impl.EncryptingInformationDispersalPersistenceServiceImpl}).
     *
     * @param informationDispersalConfig
     *          the &lt;InformationDispersal&gt; config node, which should contain a &lt;EncryptionProvider&gt;
     *          config child node.
     * @param context
     *          the IDA Persistence Engine component context.
     * @throws FileLoaderException
     */
    protected void setUpEncryptionProvider(ConfigElement informationDispersalConfig, Context context) throws FileLoaderException {
        EncryptionProvider encryptionProvider = null;

        ConfigElement encryptionProviderConfig = informationDispersalConfig.getChild("EncryptionProvider");
        if (encryptionProviderConfig != null) {
            ConfigElement classConfig = encryptionProviderConfig.getChild("Class");
            if (classConfig != null && StringUtils.isNotEmpty(classConfig.getValue())) {
                ClassLoader classLoader = getClass().getClassLoader();
                try {
                    encryptionProvider = (EncryptionProvider) classLoader.loadClass(classConfig.getValue()).newInstance();
                } catch (ClassNotFoundException e) {
                    throw new FileLoaderException("Failed to load class " + classConfig.getValue());
                } catch (Exception e) {
                    throw new FileLoaderException("Failed to instantiate " + classConfig.getValue() + ": " + e.getMessage());
                }
            } else {
                encryptionProvider = new JceEncryptionProvider();
            }

            try {
                encryptionProvider.init(new JlanConfiguration(encryptionProviderConfig, context));
            } catch (Exception e) {
                throw new FileLoaderException("Failed to initialize " + encryptionProvider.getClass().getName() + ": " + e.getMessage());
            }
        }

        if (encryptionProvider != null) {
            context.setEncryptionProvider(encryptionProvider);
        }
    }

    /**
     * Sets up the InformationDispersalPersistenceService, given the specified config.
     *
     * @param informationDispersalConfig
     *          the &lt;InformationDispersal&gt; config node, which should contain a &lt;Class&gt; config child node, to indicate
     *          the class of the service
     * @param context
     *          the IDA Persistence Engine component context.
     * @throws FileLoaderException
     */
    protected void setUpInformationDispersalPersistenceService(ConfigElement informationDispersalConfig, Context context)
            throws FileLoaderException {
        ConfigElement classConfig = informationDispersalConfig.getChild("Class");
        if (classConfig != null && StringUtils.isNotEmpty(classConfig.getValue())) {
            ClassLoader classLoader = getClass().getClassLoader();
            try {
                persistenceService = (InformationDispersalPersistenceService) classLoader.loadClass(classConfig.getValue()).newInstance();
            } catch (ClassNotFoundException e) {
                throw new FileLoaderException("Failed to load class " + classConfig.getValue());
            } catch (Exception e) {
                throw new FileLoaderException("Failed to instantiate " + classConfig.getValue() + ": " + e.getMessage());
            }
        } else {
            persistenceService = new InformationDispersalPersistenceServiceImpl();
        }

        try {
            persistenceService.init(new JlanConfiguration(informationDispersalConfig, context));
        } catch (Exception e) {
            throw new FileLoaderException("Failed to initialize " + persistenceService + ": " + e.getMessage());
        }

        context.setInformationDispersalPersistenceService(persistenceService);
    }

    /**
     * Creates the data ID for a specific file/stream. Default implementation generates a UUID. A hash can also be used,
     * but the problem is that if another file has the same hash, synchronization is probably needed.
     *
     * @param fileId
     *          the file ID
     * @param streamId
     *          the stream ID
     * @param data
     *          the actual contents
     * @return the data ID
     */
    protected String createDataId(int fileId, int streamId, byte[] data) {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns a string for the given file info. Used for logging purposes
     */
    protected String getFileInfoString(int fileId, int streamId, String dataId) {
        StringBuilder str = new StringBuilder();

        str.append("[fileId='").append(fileId).append("'");
        str.append(", streamId='").append(streamId).append("'");
        if (dataId != null) {
            str.append(", dataId='").append(dataId).append("'");
        }
        str.append("]");

        return str.toString();
    }

    /**
     * Deletes the old saved data (if any) for a specific file/stream.
     *
     * @param fileId
     *          the file ID
     * @param streamId
     *          the stream ID
     */
    protected void deleteOldSavedDataOnBackground(final int fileId, final int streamId) {
        try {
            final String dataId = getOldSavedDataId(fileId, streamId);
            if (dataId != null) {
                threadPoolExecutor.execute(new Runnable() {

                    @Override
                    public void run() {
                        if (logger.isDebugEnabled()) {
                            String fileInfoStr = getFileInfoString(fileId, streamId, dataId);
                            logger.debug("Deleting old saved data for " + fileInfoStr);
                        }

                        try {
                            int deletedNum = persistenceService.deleteData(dataId);
                            if (deletedNum == 0) {
                                String fileInfoStr = getFileInfoString(fileId, streamId, dataId);
                                logger.warn("Unable to delete old saved data for " + fileInfoStr);
                            }
                        } catch (Exception e) {
                            String fileInfoStr = getFileInfoString(fileId, streamId, dataId);
                            logger.error("Error while trying to delete old saved data for " + fileInfoStr);
                        }
                    }

                });
            }
        } catch (Exception e) {
            String fileInfoStr = getFileInfoString(fileId, streamId, null);
            logger.error("Error while trying to retrieve ID for old saved data for " + fileInfoStr);
        }
    }

    /**
     * Returns the data ID of the old saved data of the file/stream (if any).
     *
     * @param fileId
     *          the file ID
     * @param streamId
     *          the stream ID
     * @return the ID of old saved data, or null if no previously saved data.
     */
    protected String getOldSavedDataId(int fileId, int streamId) throws DBException {
        return getDBObjectIdInterface().loadObjectId(fileId, streamId);
    }

}
