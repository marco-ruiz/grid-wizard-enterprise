/*
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gwe.utils.web;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Dec 12, 2008
 */
public class HtmlTable {

	private static final String TABLE_START = "\n<table class=\"bodyTable\">\n\t<tbody>\n";
	private static final String TABLE_END = "</tbody>\n</table>";

	private static final String ROW_A_START = "\t<tr class=\"a\">\n";
	private static final String ROW_B_START = "\t<tr class=\"b\">\n";
	private static final String ROW_END = "\t</tr>\n\n";
	
	private List<HtmlTableCell> headers = new ArrayList<HtmlTableCell>();
	private List<List<HtmlTableCell>> data = new ArrayList<List<HtmlTableCell>>();
	
	public HtmlTable(Object... headers) {
		for (Object header : headers) {
			HtmlTableCell cellData = (header instanceof HtmlTableCell) ? (HtmlTableCell)header : createCell(header);
			cellData.setHeader(true);
			this.headers.add(cellData);
		}
	}
	
	public void addRow(Object... rowData) {
		List<HtmlTableCell> row = new ArrayList<HtmlTableCell>();
		data.add(row);
		for (Object dataEle : rowData) {
			HtmlTableCell cellData = null;
			if (dataEle != null) {
				if (dataEle instanceof HtmlTableCell) cellData = (HtmlTableCell)dataEle;  
				if (dataEle instanceof HtmlContent)   cellData = new HtmlTableCell("", (HtmlContent)dataEle);  
			}
			if (cellData == null) cellData = createCell(dataEle);
			row.add(cellData);
		}
	}

	private HtmlTableCell createCell(Object dataEle) {
	    return new HtmlTableCell(dataEle == null ? null : dataEle.toString(), "");
    }
	
	public boolean isEmpty() {
		return data.size() == 0;
	}
	
	public String getHTML() {
		return new StringBuffer(TABLE_START).append(getContent()).append(TABLE_END).toString(); 
	}

	private StringBuffer getContent() {
		boolean odd = true;
		StringBuffer result = getRow(ROW_A_START, headers);
		for (List<HtmlTableCell> rowData : data) {
			odd = !odd;
	        result.append(getRow(odd ? ROW_A_START : ROW_B_START, rowData));
        }
	    return result;
    }
	
	private StringBuffer getRow(String rowStart, List<HtmlTableCell> rowData) {
	    StringBuffer result = new StringBuffer(rowStart); 
		for (HtmlTableCell cellData : rowData)
			result.append(cellData.getHTML());

		return result.append(ROW_END);
    }
}
