package org.cloudraid.ida.persistance.impl;

import org.cloudraid.ida.persistance.api.FragmentRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Data structure that keeps available fragment repositories per save or load operation. That means that one instance of
 * this class is used per operation, and shouldn't be reused.
 *
 * @author avasquez
 */
public class AvailableFragmentRepositories {

    private Queue<FragmentRepository> repositories;

    public AvailableFragmentRepositories(Collection<FragmentRepository> repositories) {
        this.repositories = new LinkedList<FragmentRepository>(repositories);
    }

    /**
     * Takes any repository and removes it from the pool of repositories.
     *
     * @return a repository
     */
    public FragmentRepository takeAny() {
        return repositories.poll();
    }

    /**
     * Takes a specific repository and removes it from the pool of repositories.
     *
     * @param repositoryUrl
     *          repository URL that specified the repository to return
     * @param takeAnyIfNotFoud
     *          return any repository (removing it from the pool) if the specific one is not found
     * @return the repository specified by the URL
     */
    public FragmentRepository take(String repositoryUrl, boolean takeAnyIfNotFoud) {
        for (Iterator<FragmentRepository> iter = repositories.iterator(); iter.hasNext();) {
            FragmentRepository repository = iter.next();
            if (repository.getRepositoryUrl().equals(repositoryUrl)) {
                iter.remove();

                return repository;
            }
        }

        return takeAny();
    }

}
