package org.jigdfs.service;

import org.jigdfs.baseInterface.Listener;

public interface ServiceListener extends Listener {
    void serviceFinishedEvent(ServiceEvent event);
}
