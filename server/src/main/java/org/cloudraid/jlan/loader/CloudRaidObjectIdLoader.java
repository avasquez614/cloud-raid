package org.cloudraid.jlan.loader;

import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.filesys.db.ObjectIdFileLoader;
import org.alfresco.jlan.server.filesys.loader.FileLoaderException;
import org.alfresco.jlan.server.filesys.loader.FileSegment;
import org.alfresco.jlan.util.NameValueList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cloudraid.ida.persistance.api.FragmentMetaDataRepository;
import org.cloudraid.ida.persistance.api.FragmentRepository;
import org.cloudraid.ida.persistance.api.InformationDispersalAlgorithm;
import org.cloudraid.ida.persistance.api.InformationDispersalPersistanceService;
import org.cloudraid.ida.persistance.impl.CrsInformationDispersalAlgorithm;
import org.cloudraid.ida.persistance.impl.FilesystemFragmentRepository;
import org.cloudraid.ida.persistance.impl.InformationDispersalPersistanceServiceImpl;
import org.springframework.extensions.config.ConfigElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ObjectIdFileLoader} that uses the IDA Persistance Engine for storing/loading files.
 */
public class CloudRaidObjectIdLoader extends ObjectIdFileLoader {

    public static final int DEFAULT_REDUNDANT_FRAGMENT_NUMBER = 2;

    private static final Logger logger = Logger.getLogger(CloudRaidObjectIdLoader.class);

    protected InformationDispersalPersistanceService persistanceService;

    @Override
    public void initializeLoader(ConfigElement params, DeviceContext ctx) throws FileLoaderException, IOException {
        super.initializeLoader(params, ctx);

        ConfigElement informationDispersalConfig = params.getChild("InformationDispersal");
        if (informationDispersalConfig == null) {
            throw new FileLoaderException("CloudRaidObjectIdLoader InformationDispersal not specified");
        }

        if (!(getContext().getDBInterface() instanceof FragmentMetaDataRepository)) {
            throw new FileLoaderException("CloudRaidObjectIdLoader DBInterface should implement " +
                    FragmentMetaDataRepository.class.getName());
        }

        List<FragmentRepository> repositories = getFragmentRepositoriesFromConfig(informationDispersalConfig);
        InformationDispersalAlgorithm ida = getIdaFromConfig(informationDispersalConfig, repositories);
        File tempFragmentDir = getTempFragmentDirFromConfig(informationDispersalConfig);
        FragmentMetaDataRepository metaDataRepository = (FragmentMetaDataRepository) getContext().getDBInterface();

        persistanceService = new InformationDispersalPersistanceServiceImpl();
        persistanceService.setFragmentRepositories(repositories);
        persistanceService.setFragmentMetaDataRepository(metaDataRepository);
        persistanceService.setInformationDispersalAlgorithm(ida);
        persistanceService.setTemporaryFragmentDir(tempFragmentDir);
        try {
            persistanceService.init();
        } catch (Exception e) {
            throw new FileLoaderException("CloudRaidObjectIdLoader Failed to initialize " + persistanceService + ": " +
                    e.getMessage());
        }
    }

    @Override
    public void loadFileData(int fileId, int streamId, String objectId, FileSegment fileSeg) throws IOException {
        String dataId = createDataId(fileId, streamId);
        File dataFile = new File(fileSeg.getTemporaryFile());
        byte[] data;

        try {
            data = persistanceService.loadData(dataId);
        } catch (Exception e) {
            logger.error("Error while loading the data '" + dataId + "' through the " + persistanceService, e);

            throw new IOException("Error while loading the data '" + dataId + "' through the " + persistanceService, e);
        }

        try {
            FileUtils.writeByteArrayToFile(dataFile, data);
        } catch (IOException e) {
            logger.error("Unable to write data '" + dataId + "' to temp file " + dataFile, e);

            throw new IOException("Unable to write data '" + dataId + "' to temp file " + dataFile, e);
        }

        fileSeg.setReadableLength(data.length);
        fileSeg.signalDataAvailable();
    }

    @Override
    public String saveFileData(int fileId, int streamId, FileSegment fileSeg, NameValueList attrs) throws IOException {
        String dataId = createDataId(fileId, streamId);
        File dataFile = new File(fileSeg.getTemporaryFile());
        byte[] data;
        try {
            data = FileUtils.readFileToByteArray(dataFile);
        } catch (Exception e) {
            logger.error("Unable to read data '" + dataId + "' from temp file " + dataFile, e);

            throw new IOException("Unable to read data '" + dataId + "' from temp file " + dataFile, e);
        }

        try {
            persistanceService.saveData(dataId, data);
        } catch (Exception e) {
            logger.error("Error while saving the data '" + dataId + "' through the " + persistanceService, e);

            throw new IOException("Error while saving the data '" + dataId + "' through the " + persistanceService, e);
        }

        return dataId;
    }

    protected List<FragmentRepository> getFragmentRepositoriesFromConfig(ConfigElement informationDispersalConfig)
            throws FileLoaderException {
        List<FragmentRepository> repositories = new ArrayList<FragmentRepository>();

        ConfigElement reposConfig = informationDispersalConfig.getChild("FragmentRepositories");
        if (reposConfig == null) {
            throw new FileLoaderException("CloudRaidObjectIdLoader FragmentRepositories not specified");
        }

        Map<String, Class<?>> repositoryTypes = createDefaultRepositoryTypes();

        List<ConfigElement> reposConfigChildren = reposConfig.getChildren();
        for (ConfigElement reposConfigChild : reposConfigChildren) {
            if (reposConfigChild.getName().equals("RepositoryType")) {
                ConfigElement classConfig = reposConfigChild.getChild("Class");
                ConfigElement typeConfig = reposConfigChild.getChild("Type");

                if (classConfig == null || StringUtils.isEmpty(classConfig.getValue())) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Class for RepositoryType not specified or null");
                }
                if (typeConfig == null || StringUtils.isEmpty(typeConfig.getValue())) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Type for RepositoryType not specified or null");
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
                    throw new FileLoaderException("CloudRaidObjectIdLoader type for Repository not specified or null");
                }

                Class<?> repoClass = repositoryTypes.get(type);
                if (repoClass == null) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Unrecognized repository type '" + type + "'. " +
                            "Make sure the corresponding RepositoryType is defined before the Repository");
                }

                FragmentRepository repo;
                try {
                    repo = (FragmentRepository) repoClass.newInstance();
                } catch (Exception e) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Failed to instantiate " + repoClass.getName() +
                            ": " + e.getMessage());
                }

                ConfigElement urlConfig = reposConfigChild.getChild("Url");
                if (urlConfig == null || StringUtils.isEmpty(urlConfig.getValue())) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Url for Repository not specified or null");
                }

                repo.setRepositoryUrl(urlConfig.getValue());
                try {
                    repo.init();
                } catch (Exception e) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Failed to initialize " + repo + ": " +
                            e.getMessage());
                }

                repositories.add(repo);
            } else {
                throw new FileLoaderException("CloudRaidObjectIdLoader Unrecognized element inside FragmentRepositories: " +
                        reposConfigChild.getName());
            }
        }

        if (repositories.isEmpty()) {
            throw new FileLoaderException("CloudRaidObjectIdLoader No repositories specified");
        }

        return repositories;
    }

    protected Map<String, Class<?>> createDefaultRepositoryTypes() {
        Map<String, Class<?>> repositoryTypes = new HashMap<String, Class<?>>();
        repositoryTypes.put("file", FilesystemFragmentRepository.class);

        return repositoryTypes;
    }

    protected File getTempFragmentDirFromConfig(ConfigElement informationDispersalConfig) throws FileLoaderException {
        ConfigElement tempFragmentDirConfig = informationDispersalConfig.getChild("TempFragmentDir");
        if (tempFragmentDirConfig == null || StringUtils.isEmpty(tempFragmentDirConfig.getValue())) {
            throw new FileLoaderException("CloudRaidObjectIdLoader TempFragmentDir not specified or null");
        }

        File tempFragmentDir = new File(tempFragmentDirConfig.getValue());
        if (!tempFragmentDir.exists()) {
            try {
                FileUtils.forceMkdir(tempFragmentDir);
            } catch (Exception e) {
                throw new FileLoaderException("Failed to create TempFragmentDir" + tempFragmentDir.getAbsolutePath());
            }
        }

        return tempFragmentDir;
    }

    protected InformationDispersalAlgorithm getIdaFromConfig(ConfigElement informationDispersalConfig,
                                                             List<FragmentRepository> repositories)
            throws FileLoaderException {
        InformationDispersalAlgorithm ida = null;
        int fragmentNum = repositories.size();
        int redundantFragmentNum = DEFAULT_REDUNDANT_FRAGMENT_NUMBER;

        ConfigElement idaConfig = informationDispersalConfig.getChild("InformationDispersalAlgorithm");
        if (idaConfig != null) {
            ConfigElement classConfig = idaConfig.getChild("Class");
            ConfigElement redundantFragmentNumConfig = idaConfig.getChild("RedundantFragmentNum");

            if (classConfig != null && StringUtils.isNotEmpty(classConfig.getValue())) {
                ClassLoader classLoader = getClass().getClassLoader();
                try {
                    ida = (InformationDispersalAlgorithm) classLoader.loadClass(classConfig.getValue()).newInstance();
                } catch (ClassNotFoundException e) {
                    throw new FileLoaderException("Failed to load class " + classConfig.getValue());
                } catch (Exception e) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Failed to instantiate " + classConfig.getValue() +
                            ": " + e.getMessage());
                }
            }
            if (redundantFragmentNumConfig != null && StringUtils.isNotEmpty(redundantFragmentNumConfig.getValue())) {
                try {
                    redundantFragmentNum = Integer.parseInt(redundantFragmentNumConfig.getValue());
                } catch (Exception e) {
                    throw new FileLoaderException("CloudRaidObjectIdLoader Invalid RedundantFragmentNum: " +  e.getMessage());
                }
            }
        }

        if (redundantFragmentNum >= fragmentNum) {
            throw new FileLoaderException("CloudRaidObjectIdLoader Redundant fragment number (" + redundantFragmentNum +
                    ") can't be greater than or equal to fragment number (" + fragmentNum + ")");
        }

        if (ida == null) {
            ida = new CrsInformationDispersalAlgorithm();
        }

        ida.setFragmentNumber(fragmentNum);
        ida.setRedundantFragmentNumber(redundantFragmentNum);
        try {
            ida.init();
        } catch (Exception e) {
            throw new FileLoaderException("CloudRaidObjectIdLoader Failed to initialize " + ida + ": " + e.getMessage());
        }

        return ida;
    }

    protected String createDataId(int fileId, int streamId) {
        return "" + fileId + "$" + streamId;
    }

}
