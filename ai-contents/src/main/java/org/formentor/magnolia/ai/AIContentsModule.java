package org.formentor.magnolia.ai;

import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;

/**
 * This class is optional and represents the configuration for the ai-contents module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/ai-contents</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 * See https://documentation.magnolia-cms.com/display/DOCS/Module+configuration for information about module configuration.
 */
@Setter
@Getter
public class AIContentsModule implements info.magnolia.module.ModuleLifecycle{
    /* you can optionally implement info.magnolia.module.ModuleLifecycle */
    private String host;
    private String instruction;
    private String workspaceName;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {

    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {

    }
}
