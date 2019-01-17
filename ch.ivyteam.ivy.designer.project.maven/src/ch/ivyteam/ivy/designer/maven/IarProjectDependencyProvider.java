package ch.ivyteam.ivy.designer.maven;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.ArtifactRef;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;

import ch.ivyteam.di.restricted.DiCore;
import ch.ivyteam.ivy.library.ILibraryConfiguration;
import ch.ivyteam.ivy.persistence.ivyarchive.IvyArchiveConstants;
import ch.ivyteam.ivy.persistence.ivyarchive.IvyArchiveUtils;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.project.IIvyProjectManager;
import ch.ivyteam.ivy.project.IvyProjectNavigationUtil;
import ch.ivyteam.log.Logger;

public class IarProjectDependencyProvider implements IMavenProjectChangedListener
{
  private static final Logger LOGGER = Logger.getLogger(IarProjectDependencyProvider.class);
  private IIvyProjectManager ivyProjectManager;

  public IarProjectDependencyProvider()
  {
    ivyProjectManager = DiCore.getGlobalInjector().getInstance(IIvyProjectManager.class);
  }

  @Override
  public void mavenProjectChanged(MavenProjectChangedEvent[] events, IProgressMonitor monitor)
  {
    MavenProjectChangedEvent first = events[0];

    Set<Artifact> artifacts = first.getMavenProject().getMavenProject().getArtifacts();
    Set<Artifact> iars = artifacts.stream()
            .filter(a -> a.getType().equals(IvyArchiveConstants.FILE_EXTENSION))
            .collect(Collectors.toSet());
    provideIarDepsToWorkspace(iars);

    if (first.getOldMavenProject() != null)
    {
      Set<ArtifactKey> removedArtifacts = findRemovedArtifacts(
              first.getMavenProject().getMavenProjectArtifacts(),
              first.getOldMavenProject().getMavenProjectArtifacts());
      removeIarDepsFromWorkspace(removedArtifacts);
    }
  }

  private static Set<ArtifactKey> findRemovedArtifacts(Set<ArtifactRef> current, Set<ArtifactRef> old)
  {
    Set<ArtifactKey> currentKeys = current.stream().map(ref -> ref.getArtifactKey()).collect(Collectors.toSet());
    Set<ArtifactKey> removed = old.stream()
            .map(ref -> ref.getArtifactKey()).filter(key -> !currentKeys.contains(key))
            .collect(Collectors.toSet());
    return removed;
  }

  void removeIarDepsFromWorkspace(Set<ArtifactKey> removed)
  {
    List<IIvyProject> deletableProjects = findWsProjectForArtifactKey(removed);
    removeFromWorskpace(deletableProjects);
  }

  private void removeFromWorskpace(List<IIvyProject> deletableProjects)
  {
    for (IIvyProject project : deletableProjects)
    {
      try
      {
        project.getProject().delete(false, true, new NullProgressMonitor());
      }
      catch (CoreException ex)
      {
        LOGGER.error("Failed to automatically remove iar '"+project+"' which is no longer required by another maven project.");
      }
    }
  }

  private List<IIvyProject> findWsProjectForArtifactKey(Set<ArtifactKey> removed)
  {
    List<IIvyProject> deletableProjects = new ArrayList<>();
    for(ArtifactKey key : removed)
    {
      IIvyProject project = findIvyProject(key);
      if (project != null && IvyArchiveUtils.isIvyArchiveProject(project.getProject()))
      {
        // TODO check no other deps
        deletableProjects.add(project);
      }
    }
    return deletableProjects;
  }

  private IIvyProject findIvyProject(ArtifactKey key)
  {
    for(IIvyProject project : ivyProjectManager.getIvyProjects())
    {
      if (key.equals(getKey(project)))
      {
        return project;
      }
    }
    return null;
  }

  private static ArtifactKey getKey(IIvyProject project)
  {
    try
    {
      ILibraryConfiguration library = project.getLibrary(null);
      return new ArtifactKey(library.getId().groupId(), library.getId().id(), library.getVersion().getRaw(), null);
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  void provideIarDepsToWorkspace(Set<Artifact> iars)
  {
    for (Artifact artifact : iars)
    {
      if (findWsProjectForArtifact(artifact) == null)
      {
        addToWorkspace(artifact);
      }
    }
  }

  private void addToWorkspace(Artifact artifact)
  {
    try
    {
      IIvyProject addedIarDep = ivyProjectManager.importer().ivyArchives(artifact.getFile()).run().ivyProject();
      LOGGER.debug("Added '"+addedIarDep+"' from maven repository");
    }
    catch (Exception ex)
    {
      LOGGER.error("Failed to provide '"+artifact+"' from maven repository");
    }
  }

  private static IProject findWsProjectForArtifact(Artifact artifact)
  {
    ArtifactKey keyToFind = new ArtifactKey(artifact);
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
    for(IProject project : projects)
    {
      IIvyProject ivyProject = IvyProjectNavigationUtil.getIvyProject(project);
      if (ivyProject == null)
      {
        continue;
      }
      if (keyToFind.equals(getKey(ivyProject)))
      {
        return project;
      }
    }
    return null;
  }
}