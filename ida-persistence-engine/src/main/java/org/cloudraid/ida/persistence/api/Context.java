package org.cloudraid.ida.persistence.api;

import org.cloudraid.ida.persistence.crypto.EncryptionKeyRepository;
import org.cloudraid.ida.persistence.crypto.EncryptionProvider;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Context used for accessing the different components of the module.
 */
public class Context {

    private InformationDispersalPersistenceService informationDispersalPersistenceService;
    private List<FragmentRepository> fragmentRepositories;
    private FragmentMetaDataRepository fragmentMetaDataRepository;
    private InformationDispersalAlgorithm informationDispersalAlgorithm;
    private EncryptionProvider encryptionProvider;
    private EncryptionKeyRepository encryptionKeyRepository;
    private Executor threadPoolExecutor;

    public InformationDispersalPersistenceService getInformationDispersalPersistenceService() {
        return informationDispersalPersistenceService;
    }

    public void setInformationDispersalPersistenceService(InformationDispersalPersistenceService informationDispersalPersistenceService) {
        this.informationDispersalPersistenceService = informationDispersalPersistenceService;
    }

    public List<FragmentRepository> getFragmentRepositories() {
        return fragmentRepositories;
    }

    public void setFragmentRepositories(List<FragmentRepository> fragmentRepositories) {
        this.fragmentRepositories = fragmentRepositories;
    }

    public FragmentMetaDataRepository getFragmentMetaDataRepository() {
        return fragmentMetaDataRepository;
    }

    public void setFragmentMetaDataRepository(FragmentMetaDataRepository fragmentMetaDataRepository) {
        this.fragmentMetaDataRepository = fragmentMetaDataRepository;
    }

    public InformationDispersalAlgorithm getInformationDispersalAlgorithm() {
        return informationDispersalAlgorithm;
    }

    public void setInformationDispersalAlgorithm(InformationDispersalAlgorithm informationDispersalAlgorithm) {
        this.informationDispersalAlgorithm = informationDispersalAlgorithm;
    }

    public Executor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public EncryptionProvider getEncryptionProvider() {
        return encryptionProvider;
    }

    public void setEncryptionProvider(EncryptionProvider encryptionProvider) {
        this.encryptionProvider = encryptionProvider;
    }

    public EncryptionKeyRepository getEncryptionKeyRepository() {
        return encryptionKeyRepository;
    }

    public void setEncryptionKeyRepository(EncryptionKeyRepository encryptionKeyRepository) {
        this.encryptionKeyRepository = encryptionKeyRepository;
    }

    public void setThreadPoolExecutor(Executor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

}
