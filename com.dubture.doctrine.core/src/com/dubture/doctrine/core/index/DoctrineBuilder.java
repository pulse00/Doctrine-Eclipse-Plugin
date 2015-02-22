package com.dubture.doctrine.core.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.dubture.doctrine.core.DoctrineCorePlugin;
import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.Entity;
import com.dubture.doctrine.core.preferences.DoctrineCoreConstants;

/**
 * This builder parses .xml files for doctrine ORM mappings. As
 * DLTK does not allow to index non-script sources, the entities
 * are stored in a "pendingEntities" list, which will be fetched
 * during dltk indexing by the {@link DoctrineIndexingVisitorExtension}
 * where they are indexed using DLTK.
 * 
 */
public class DoctrineBuilder extends IncrementalProjectBuilder
{
    public static final String BUILDER_ID = DoctrineCorePlugin.ID + ".doctrineBuilder";

    /**
     * Storage for found entity mappings.
     */
    private static final List<Entity> pendingEntities = new ArrayList<Entity>();
    
    
    public static List<Entity> getPendingEntities()
    {
        List<Entity> entities = new ArrayList<Entity>();
        synchronized (pendingEntities) {
            entities.addAll(pendingEntities);
            pendingEntities.clear();
        }
        return entities;
    }
    
    public static void addPendingEntity(Entity entity) 
    {
        synchronized (pendingEntities) {
            pendingEntities.add(entity);
        }
    }
    
    
    @Override
    protected IProject[] build(int kind, Map<String, String> args,
            IProgressMonitor monitor) throws CoreException
    {
        if (kind == FULL_BUILD) {
            fullBuild(monitor);
        } else {
            IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                fullBuild(monitor);
            } else {
                incrementalBuild(delta, monitor);
            }
        }
        return null;
    }
    
    protected void fullBuild(final IProgressMonitor monitor)
            throws CoreException {
        try {
            getProject().accept(new ResourceVisitor());
        } catch (Exception e) {
            Logger.logException(e);
        }
    }
    
    protected void incrementalBuild(IResourceDelta delta,
            IProgressMonitor monitor) throws CoreException {
        delta.accept(new ResourceVisitor());
    }   

}
