package org.cloudraid.ida.persistance.impl;

import org.cloudraid.ida.persistance.api.FragmentRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Data structure that keeps available repositories per save or load operation. That means that one instance of this class is used per
 * operation, and shouldn't be reused.
 *
 * @author avasquez
 */
public class AvailableFragmentRepositoryCollection {

    private Queue<FragmentRepository> repositories;

    public AvailableFragmentRepositoryCollection(Collection<FragmentRepository> repositories) {
        this.repositories = new LinkedList<FragmentRepository>(repositories);
    }

    /**
     * Takes any repository and removes it from the collection of repositories.
     *
     * @return a repository
     */
    public FragmentRepository take() {
        return repositories.poll();
    }

    /**
     * Takes a specific repository and removes it from the collection of repositories.
     *
     * @param repositoryUrl
     *
     * @return
     */
    public FragmentRepository take(String repositoryUrl) {
        for (Iterator<FragmentRepository> iter = repositories.iterator(); iter.hasNext();) {
            FragmentRepository repository = iter.next();
            if (repository.getRepositoryUrl().equals(repositoryUrl)) {
                iter.remove();

                return repository;
            }
        }

        return null;
    }

}
