package org.sagebionetworks.web.client.widget.table.api;

import java.util.List;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface APITableWidgetView extends IsWidget, SynapseView {
	void setPresenter(Presenter presenter);
	
	/**
	 * Call to add the pager bar to the table
	 * @param start
	 * @param end
	 * @param total
	 */
	public void configurePager(int start, int end, int total);
	
	public void showError(IsWidget synAlert);
	
	void showTableUnavailable();
	void setColumnHeaderNames(List<String> headers);
	void addRow(List<IsWidget> columnWidgets);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void pageBack();
		void pageForward();
		void columnClicked(int index);
		void refreshData();
	}
}
