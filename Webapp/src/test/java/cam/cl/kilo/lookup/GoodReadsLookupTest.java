/*
 * Copyright (C) 2015 Group Kilo (Cambridge Computer Lab)
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

package cam.cl.kilo.lookup;

import cam.cl.kilo.NLP.ItemInfo;
import org.junit.Ignore;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * @author groupKilo
 * @author yy363
 */
public class GoodReadsLookupTest {

	public GoodReadsLookupTest() {
	}

    @Ignore
	@Test
	public void testGoodReadsLookUp() {
		ItemInfo info = new ItemInfo();
		GoodReadsLookup test = new GoodReadsLookup("isbn", "978-0764570681", info);
	}

    @Ignore
	@Test
	public void testFillContent() {
	}

	@Test
	public void testGetTitle() {
		ItemInfo info = new ItemInfo();
		GoodReadsLookup test = new GoodReadsLookup("978-0764570681", "ISBN", info);
        test.run();

		assertEquals("C for Dummies", info.getTitle());
	}


    @Ignore
	@Test
	public void testGetDescription() {
		ItemInfo info = new ItemInfo();
		GoodReadsLookup test = new GoodReadsLookup("978-0764570681", "ISBN", info);
		assertEquals("description", test.info.getDescriptions().get(0));
	}

	/*
	 * Need to perform a check on multiple authors (ordering)
	 * Only 1 author has been tested so far
	 */
    @Ignore
	@Test
	public void testGetAuthors() {
		ItemInfo info = new ItemInfo();
		GoodReadsLookup test = new GoodReadsLookup("isbn", "978-0764570681", info);
		List<String> authors = new LinkedList<String>();
		authors.add("Dan Gookin");
		assertEquals(authors, test.info.getAuthors());
	}

}
