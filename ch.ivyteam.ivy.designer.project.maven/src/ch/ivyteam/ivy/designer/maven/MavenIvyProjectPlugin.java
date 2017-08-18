package ch.ivyteam.ivy.designer.maven;

import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class MavenIvyProjectPlugin extends AbstractUIPlugin implements IStartup
{
  public static final String PLUGIN_ID = "ch.ivyteam.ivy.designer.project.maven"; //$NON-NLS-1$
  private static MavenIvyProjectPlugin plugin;

  private IarProjectDependencyProvider listener;

  public MavenIvyProjectPlugin()
  {
  }

  @Override
  public void start(BundleContext context) throws Exception
  {
    super.start(context);

    this.listener = new IarProjectDependencyProvider();
    MavenPluginActivator.getDefault().getMavenProjectManagerImpl()
            .addMavenProjectChangedListener(listener);

    plugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception
  {
    plugin = null;
    if (listener != null)
    {
      MavenPluginActivator.getDefault().getMavenProjectManagerImpl()
      .removeMavenProjectChangedListener(listener);
    }
    super.stop(context);
  }

  public static MavenIvyProjectPlugin getDefault()
  {
    return plugin;
  }

  @Override
  public void earlyStartup()
  { // triggers an explicit plugin start when the designer starts!
  }

}
