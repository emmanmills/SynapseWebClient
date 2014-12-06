package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.StringUtils;

import com.google.inject.Inject;

/**
 * Table editor for columns of type DOUBLE.
 * 
 * @author jhill
 *
 */
public class DoubleCellEditorImpl extends AbstractCellEditor implements DoubleCellEditor{

	@Inject
	public DoubleCellEditorImpl(CellEditorView view) {
		super(view);
	}

	@Override
	public boolean isValid() {
		String value = StringUtils.trimWithEmptyAsNull(this.getValue());
		if(value != null){
			try{
				// if it parses it is valid.
				Double.parseDouble(value);
			}catch(NumberFormatException e){
				view.setValidationState(ValidationState.ERROR);
				view.setHelpText(e.getMessage());
				return false;
			}
		}
		view.setValidationState(ValidationState.NONE);
		view.setHelpText("");
		return true;
	}

}
