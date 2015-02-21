package com.dubture.doctrine.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.CompletionRequestor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.WorkingCopyOwner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.php.core.codeassist.ICompletionContextResolver;
import org.eclipse.php.core.codeassist.ICompletionStrategyFactory;
import org.eclipse.php.core.tests.PHPCoreTests;
import org.eclipse.php.core.tests.filenetwork.FileUtil;
import org.eclipse.php.core.tests.runner.AbstractPDTTRunner.Context;
import org.eclipse.php.core.tests.runner.PDTTList.AfterList;
import org.eclipse.php.core.tests.runner.PDTTList.BeforeList;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.codeassist.AliasType;
import org.eclipse.php.internal.core.codeassist.IPHPCompletionRequestorExtension;
import org.eclipse.php.internal.core.facet.PHPFacets;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.eclipse.php.internal.core.typeinference.FakeConstructor;
import org.junit.After;
import org.osgi.framework.Bundle;

import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.codeassist.DoctrineCompletionContextResolver;
import com.dubture.doctrine.core.codeassist.DoctrineCompletionStrategyFactory;
import com.dubture.doctrine.test.CodeAssistPdttFile.ExpectedProposal;

@SuppressWarnings("restriction")
abstract public class AbstractCodeAssistTest {

	protected boolean displayName = false;
	protected String endChar = ",";

	// infos for invalid results
	protected int tabs = 2;

	// working copies usage
	protected ISourceModule[] workingCopies;
	protected WorkingCopyOwner wcOwner;
	protected boolean discard;
	protected final String projectName;
	protected final String stubsDir;
	protected final char OFFSET_CHAR = '|';

	protected IProject project;
	protected IFile testFile;
	protected String[] fileNames;

	@BeforeList
	public void setUpSuite() throws Exception {

		if (this.discard) {
			this.workingCopies = null;
		}

		this.discard = true;

		if (project != null && project.exists()) {
			return;
		}

		project = setUpProject();

		IProjectDescription desc = project.getDescription();
		desc.setNatureIds(new String[] { PHPNature.ID, DoctrineNature.NATURE_ID });
		project.setDescription(desc, null);

		ProjectOptions.setPhpVersion(PHPVersion.getLatestVersion(), project);

		PHPFacets.setFacetedVersion(project, PHPVersion.getLatestVersion());

		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		PHPCoreTests.waitForIndexer();
		PHPCoreTests.waitForAutoBuild();

	}

	@AfterList
	public void tearDownSuite() throws Exception {

		if (this.discard && this.workingCopies != null) {
			discardWorkingCopies(this.workingCopies);
			this.wcOwner = null;
		}

		if (testFile != null) {
			testFile.delete(true, null);
			testFile = null;
		}

		for (IResource res : project.members()) {
			if (res.getName().startsWith(".")) {
				continue;
			}
			res.delete(true, null);
		}

		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.close(null);
		project.delete(true, true, null);
		project = null;
	}

	public AbstractCodeAssistTest(String[] fileNames, String projectName, String stubsDir) {
		this.fileNames = fileNames;
		this.projectName = projectName;
		this.stubsDir = stubsDir;
	}

	/**
	 * Creates test file with the specified content and calculates the offset at
	 * OFFSET_CHAR. Offset character itself is stripped off.
	 *
	 * @param data
	 *            File data
	 * @return offset where's the offset character set.
	 * @throws Exception
	 */
	protected int createFile(String data) throws Exception {
		int offset = data.lastIndexOf(OFFSET_CHAR);
		if (offset == -1) {
			throw new IllegalArgumentException("Offset character is not set");
		}

		// replace the offset character
		data = data.substring(0, offset) + data.substring(offset + 1);

		testFile = project.getFile(UUID.randomUUID().toString() + ".php");
		testFile.create(new ByteArrayInputStream(data.getBytes()), true, null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		PHPCoreTests.waitForIndexer();

		return offset;
	}

	protected ISourceModule getSourceModule() {
		return DLTKCore.createSourceModuleFrom(testFile);
	}

	public CompletionProposal[] getProposals(String data) throws Exception {
		int offset = createFile(data);
		return getProposals(offset);
	}

	public CompletionProposal[] getProposals(int offset) throws ModelException {
		return getProposals(getSourceModule(), offset);
	}

	abstract class TestCompletionRequestor extends CompletionRequestor implements IPHPCompletionRequestorExtension {

		@Override
		public ICompletionContextResolver[] getContextResolvers() {
			List<ICompletionContextResolver> asList = new ArrayList<ICompletionContextResolver>();
			asList.add(new DoctrineCompletionContextResolver());
			return asList.toArray(new ICompletionContextResolver[asList.size()]);
		}

		@Override
		public ICompletionStrategyFactory[] getStrategyFactories() {
			List<ICompletionStrategyFactory> asList = new ArrayList<ICompletionStrategyFactory>();
			asList.add(new DoctrineCompletionStrategyFactory());
			return asList.toArray(new ICompletionStrategyFactory[asList.size()]);
		}

	}

	public CompletionProposal[] getProposals(ISourceModule sourceModule, int offset) throws ModelException {
		final List<CompletionProposal> proposals = new LinkedList<CompletionProposal>();
		sourceModule.codeComplete(offset, new TestCompletionRequestor() {
			public void accept(CompletionProposal proposal) {
				proposals.add(proposal);
			}
		});

		return (CompletionProposal[]) proposals.toArray(new CompletionProposal[proposals.size()]);
	}

	public void compareProposals(CompletionProposal[] proposals, CodeAssistPdttFile pdttFile) throws Exception {

		ExpectedProposal[] expectedProposals = pdttFile.getExpectedProposals();

		boolean proposalsEqual = true;
		if (proposals.length == expectedProposals.length) {
			for (ExpectedProposal expectedProposal : pdttFile.getExpectedProposals()) {
				boolean found = false;
				for (CompletionProposal proposal : proposals) {
					IModelElement modelElement = proposal.getModelElement();

					if (modelElement == null) {
						if (new String(proposal.getName().trim()).equalsIgnoreCase(expectedProposal.name)) { // keyword
							found = true;
							break;
						}
					} else if (modelElement.getElementType() == expectedProposal.type) {
						if (modelElement instanceof AliasType) {
							if (((AliasType) modelElement).getAlias().trim().equals(expectedProposal.name)) {
								found = true;
								break;
							}
						} else if ((modelElement instanceof FakeConstructor) && (modelElement.getParent() instanceof AliasType)) {
							if (((AliasType) modelElement.getParent()).getAlias().trim().equals(expectedProposal.name)) {
								found = true;
								break;
							}
						} else {
							if (modelElement.getElementName().trim().equalsIgnoreCase(expectedProposal.name)) {
								found = true;
								break;
							}
						}
					} else if (modelElement.getElementType() == expectedProposal.type
							&& new String(proposal.getName()).trim().equalsIgnoreCase(expectedProposal.name)) {
						// for phar include
						found = true;
						break;
					}
				}
				if (!found) {
					proposalsEqual = false;
					break;
				}
			}
		} else {
			proposalsEqual = false;
		}

		if (!proposalsEqual) {
			StringBuilder errorBuf = new StringBuilder();
			errorBuf.append("\nEXPECTED COMPLETIONS LIST:\n-----------------------------\n");
			errorBuf.append(pdttFile.getExpected());
			errorBuf.append("\nACTUAL COMPLETIONS LIST:\n-----------------------------\n");
			for (CompletionProposal p : proposals) {
				IModelElement modelElement = p.getModelElement();
				if (modelElement == null || modelElement.getElementName() == null) {
					errorBuf.append("keyword(").append(p.getName()).append(")\n");
				} else {
					switch (modelElement.getElementType()) {
					case IModelElement.FIELD:
						errorBuf.append("field");
						break;
					case IModelElement.METHOD:
						errorBuf.append("method");
						break;
					case IModelElement.TYPE:
						errorBuf.append("type");
						break;
					}
					if (modelElement instanceof AliasType) {
						errorBuf.append('(').append(((AliasType) modelElement).getAlias()).append(")\n");
					} else {
						errorBuf.append('(').append(modelElement.getElementName()).append(")\n");
					}
				}
			}
			fail(errorBuf.toString());
		}
	}

	protected IProject setUpProject() throws CoreException, IOException {
		return setUpProjectTo(projectName, stubsDir);
	}

	protected IProject setUpProjectTo(final String projectName, final String fromName) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		final File sourceWorkspacePath = getSourceWorkspacePath();
		final File targetWorkspacePath = getWorkspaceRoot().getLocation().toFile();
		final File source = new File(sourceWorkspacePath, fromName);
		if (!source.isDirectory()) {
			throw new IllegalArgumentException(NLS.bind("Source directory \"{0}\" doesn't exist", source));
		}
		copyDirectory(source, new File(targetWorkspacePath, projectName));
		return createProject(projectName);
	}

	public File getSourceWorkspacePath() {
		return new File(getPluginDirectoryPath(), "/");
	}
	
	@Context
	 public static Bundle getContext() {
		 return DoctrineTestPlugin.getDefault().getBundle();
	 }

	protected File getPluginDirectoryPath() {
		try {
			final Bundle bundle = getContext();
			URL platformURL = bundle.getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void copyDirectory(File source, File target) throws IOException {
		FileUtil.copyDirectory(source, target);
	}

	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}

	public IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}

	protected IProject createProject(final String projectName) throws CoreException {
		final IProject project = getProject(projectName);
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(create, null);
		return project;
	}

	protected void discardWorkingCopies(ISourceModule[] units) throws ModelException {
		if (units == null)
			return;
		for (int i = 0, length = units.length; i < length; i++)
			if (units[i] != null)
				units[i].discardWorkingCopy();
	}

	protected void runPdttTest(String filename) throws Exception {
		final CodeAssistPdttFile pdttFile = new CodeAssistPdttFile(getContext(), filename);
		CompletionProposal[] proposals = getProposals(pdttFile.getFile());
		compareProposals(proposals, pdttFile);
	}
	@After
	public void after() throws CoreException 
	{
		if (testFile != null) {
			testFile.delete(true, null);
			testFile = null;
		}
	}
}
