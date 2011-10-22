package com.dubture.doctrine.ui.dialog;

import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.dltk.internal.ui.dialogs.MethodSelectionComponent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

@SuppressWarnings("restriction")
public class GetterSetterDialog extends SelectionStatusDialog {

	private SourceType type;
	public GetterSetterDialog(Shell parent, SourceType type) {
		super(parent);
		
		this.type = type;
		

	}
	
	private class TitleLabel implements MethodSelectionComponent.ITitleLabel {

		@Override
		public void setText(String text) {

			
		}
	}
	
	protected Control createDialogArea(Composite parent) {
		
		Composite area = (Composite) super.createDialogArea(parent);

		return area;
		
	}
	
	
	@Override
	protected void computeResult() {
		// TODO Auto-generated method stub

	}

}
