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

package org.gwe.app.daemon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.app.daemon.domain.UserDomain;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.order.p2el.POrderDescriptor;

/**
 * @author Marco Ruiz
 * @since Nov 24, 2008
 */
public class QueueOrderTest {

	private static Log log = LogFactory.getLog(QueueOrderTest.class);

	public QueueOrderTest(final UserDomain domain) {
	    new Thread(new Runnable() {
			public void run() {
				OrderInfo order = new OrderInfo(new POrderDescriptor<String>("${INDEX_1}=$range(1,4) ${INDEX_2}=$range(1,4) ${FASTA_FILE}=$in(http://www.expasy.org/uniprot/Q70${INDEX_1}S${INDEX_2}.fas) cat ${FASTA_FILE}"));
				order = domain.persistOrder(order); 
				domain.queueOrder(order);
				log.info("Request to queue an order received: " + order);
				int result = order.getId();
				return;
			}
	    }).start();
	}
}
