package org.cloudraid.ida.persistence.impl;

import org.cloudraid.ida.persistence.api.FragmentRepository;

import java.util.Collection;
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
    public FragmentRepository take() {
        return repositories.poll();
    }

}
