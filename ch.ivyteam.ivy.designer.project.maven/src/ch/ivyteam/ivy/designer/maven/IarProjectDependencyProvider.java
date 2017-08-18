package ch.ivyteam.ivy.designer.maven;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.embedder.ArtifactRef;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;

import ch.ivyteam.ivy.designer.ide.DesignerIDEPlugin;
import ch.ivyteam.ivy.library.ILibraryConfiguration;
import ch.ivyteam.ivy.persistence.PersistencyException;
import ch.ivyteam.ivy.persistence.ivyarchive.IvyArchiveUtils;
import ch.ivyteam.ivy.project.IIvyProject;
import ch.ivyteam.ivy.resource.datamodel.ResourceDataModelException;

public class IarProjectDependencyProvider implements IMavenProjectChangedListener
{

  @Override
  public void mavenProjectChanged(MavenProjectChangedEvent[] events, IProgressMonitor monitor)
  {
    MavenProjectChangedEvent first = events[0];
    findRemovedArtifacts(first.getMavenProject().getMavenProjectArtifacts(), first.getOldMavenProject().getMavenProjectArtifacts());
    //first.getOldMavenProject().getMavenProjectArtifacts()
    Set<Artifact> artifacts = first.getMavenProject().getMavenProject().getArtifacts();
    Set<DefaultArtifact> iars = artifacts.stream()
            .filter(a -> a instanceof DefaultArtifact)
            .map(a -> (DefaultArtifact) a)
            .filter(a -> a.getType().equals("iar"))
            .collect(Collectors.toSet());
    newIarDeps(first.getSource().getProject(), iars);
  }

  private void findRemovedArtifacts(Set<ArtifactRef> current, Set<ArtifactRef> old)
  {
    Set<ArtifactKey> currentKeys = current.stream().map(ref -> ref.getArtifactKey()).collect(Collectors.toSet());
    Set<ArtifactKey> removed = old.stream()
            .map(ref -> ref.getArtifactKey()).filter(key -> !currentKeys.contains(key))
            .collect(Collectors.toSet());
    
    List<IIvyProject> deletableProjects = new ArrayList<>();
    for(ArtifactKey key : removed)
    {
      IIvyProject project = findIvyProject(key);
      if (project != null && IvyArchiveUtils.isIvyArchiveProject(project.getProject()))
      {
        // check no other deps
        deletableProjects.add(project);
      }
    }
    for (IIvyProject project : deletableProjects)
    {
      try
    {
      project.getProject().delete(false, true, new NullProgressMonitor());
    }
    catch (CoreException ex)
    {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }
    }
    
    System.out.println(current);
  }

  private IIvyProject findIvyProject(ArtifactKey key)
  {
    for(IIvyProject project : DesignerIDEPlugin.getDefault().getProjectManager().getIvyProjects())
    {
      try
      {
        ILibraryConfiguration library = project.getLibrary(null);
        ArtifactKey libraryKey = new org.eclipse.m2e.core.embedder.ArtifactKey(library.getId().groupId(), library.getId().id(), library.getVersion().getRaw(), null);
        if (libraryKey.equals(key))
        {
          return project;
        }
      }
      catch (ResourceDataModelException ex)
      {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      }
    }
    return null;
  }

  private void newIarDeps(IProject project, Set<DefaultArtifact> iars)
  {
    for (DefaultArtifact artifact : iars)
    {
      System.out.println("i depend on " + artifact);
      if (findProject(project.getWorkspace(), artifact.getFile()) == null)
      {
        try
        {
          IIvyProject addedIarDep = DesignerIDEPlugin.getDefault().getProjectManager().createIvyArchiveProject(artifact.getFile(), new NullProgressMonitor());
          addedIarDep.toString();
        }
        catch (PersistencyException | CoreException | IOException ex)
        {
          // TODO Auto-generated catch block
          ex.printStackTrace();
        }
      }
    }
    // TODO Auto-generated method stub
    System.out.println("changed...");
  }
  
  private IProject findProject(IWorkspace ws, File iar)
  {
    URI uri = IvyArchiveUtils.getIvyArchiveRootUri(iar);
    IProject[] projects = ws.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
    for(IProject project : projects)
    {
      if (uri.equals( project.getLocationURI()))
      {
        return project;
      }
    }
    return null;
  }
}