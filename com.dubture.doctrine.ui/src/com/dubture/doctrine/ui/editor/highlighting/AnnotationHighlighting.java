/*******************************************************************************
 * This file is part of the Doctrine eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.ui.editor.highlighting;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.ui.Logger;
import org.eclipse.php.internal.ui.editor.highlighter.AbstractSemanticApply;
import org.eclipse.php.internal.ui.editor.highlighter.AbstractSemanticHighlighting;
import org.eclipse.swt.graphics.RGB;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.parser.antlr.SourcePosition;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;

/** 
 *
 * Highlighting for Annotations.
 *
 */
@SuppressWarnings("restriction")
public class AnnotationHighlighting extends AbstractSemanticHighlighting {

    protected class AnnotationApply extends AbstractSemanticApply {

        protected ISourceModule sourceModule;
        protected IAnnotationModuleDeclaration decl;

        public AnnotationApply() {
            this.sourceModule = getSourceModule();
            try {
				this.decl = AnnotationParserUtil.getModule(sourceModule);
			} catch (CoreException e) {
				com.dubture.doctrine.ui.log.Logger.logException(e);
			}
        }
        
        @Override
		public boolean visit(Program program) {
        	if (decl == null) {
        		return false;
        	}
			try {
				getSourceModule().accept(new IModelElementVisitor() {

					@Override
					public boolean visit(IModelElement element) {
						if (element instanceof IMember) {
							List<Annotation> annotations = decl.readAnnotations((IMember)element).getAnnotations();
				            for (Annotation annotation : annotations) {
				                SourcePosition sourcePosition = annotation.getSourcePosition();
				                highlight(sourcePosition.startOffset, sourcePosition.length);
				            }
						}
						return true;
					}
				});

			} catch (ModelException e) {
				Logger.logException(e);
			}
			return false;
		}
    }
    

    public AnnotationHighlighting() {
        super();
    }

    @Override
    public String getDisplayName() {
        return "Annotations";
    }

    @Override
    public AbstractSemanticApply getSemanticApply() {
        return new AnnotationApply();
    }

    @Override
    public void initDefaultPreferences() {
        getStyle().setUnderlineByDefault(false).setDefaultTextColor(
                new RGB(64, 64, 64));
    }
    
}
