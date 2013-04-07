/*******************************************************************************
 * This file is part of the Symfony eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.annotation.parser.tree.visitor;

import com.dubture.doctrine.annotation.parser.tree.AnnotationCommonTree;



/**
 * Interface for AnnotationNodeVisitor concrete classes.
 *
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
public interface IAnnotationNodeVisitor {
    void visit(AnnotationCommonTree node);
}
