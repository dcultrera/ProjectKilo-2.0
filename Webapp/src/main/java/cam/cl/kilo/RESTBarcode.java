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

package cam.cl.kilo;

import cam.cl.kilo.lookup.AmznItemLookup;
import cam.cl.kilo.lookup.GoodReadsLookup;
import cam.cl.kilo.lookup.OMDBLookup;
import cam.cl.kilo.NLP.ItemInfo;
import cam.cl.kilo.NLP.MEADSummarizer;
import cam.cl.kilo.NLP.Summary;
import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Main class of the backend:
 * it populates an ItemInfo object by starting threads that look up product information and reviews;
 * it isummarizes text retrieved;
 * it serializes summary for transmission to the frontend
 *
 * @author groupKilo
 * @author rh572
 * @author dc561
 */
@Path("/barcode")
public class RESTBarcode {

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response produceResponse(
            @QueryParam("barcodeNo") String barcodeNo,
            @DefaultValue("ISBN") @QueryParam("barcodeType") String barcodeType) {

		String encodedSummary;
        Summary summary;

		if (barcodeNo != null) {

			//Make API calls and gather item information
            ItemInfo info = populateItemInfo(barcodeNo, barcodeType);

            // Summarize text in ItemInfo and create a Summary object
            summary = generateSummary(info);

        } else {
            summary = new Summary("Missing barcode number. Try scanning again.");
        }

        // Serialize Summary object
        try {
            encodedSummary = toString(summary);
        } catch (IOException e) {
            e.printStackTrace();
            encodedSummary = e.getMessage();
        }

        return Response.ok(encodedSummary).build();
    }


    /**
     * Make different API calls according to the item's type
     *
     * @param barcodeNo The item's barcode number
     * @param barcodeType The type of the barcode (ISBN, UPC or other)
     * @return ItemInfo object with item information
     */
    public static ItemInfo populateItemInfo(String barcodeNo, String barcodeType) {
        ItemInfo info = new ItemInfo();

        Thread tAmzn = new Thread(new AmznItemLookup(barcodeNo, barcodeType, info));
        Thread tGR = new Thread(new GoodReadsLookup(barcodeNo, barcodeType, info));

        tAmzn.start();

        if ("ISBN".equals(barcodeType)) tGR.start();

        while(tAmzn.isAlive() || tGR.isAlive());

        if (!"ISBN".equals(barcodeType)) {
            Thread tOMDB = new Thread(new OMDBLookup(barcodeNo, barcodeType, info));
            while(tOMDB.isAlive());
        }

        return info;
    }


    /**
     * Given an ItemInfo, initialise two Summarizer objects, one for descriptions and one for reviews.
     * In case of MEAD failure or other errors, the full text is returned.
     *
     * @param info A populated ItemInfo
     * @return A Summary object holding summarized text
     */
    public static Summary generateSummary(ItemInfo info) {
        MEADSummarizer descriptionSummarizer, reviewSummarizer;
        String summarizedDescriptions, summarizedReviews;

        // Handle both summarizers in same try/catch: if there is an IO error from MEAD, it is likely to affect both
        try {
            descriptionSummarizer = new MEADSummarizer(info.getDescriptions(), 5);
            reviewSummarizer = new MEADSummarizer(info.getReviews(), 5);

            if (! descriptionSummarizer.isEmpty()) {
                summarizedDescriptions = descriptionSummarizer.getSummary();
                System.out.println("Description summarization successful");
            } else {
                try {
                    summarizedDescriptions = info.getDescriptions().firstElement();
                } catch (NoSuchElementException nsee) {
                    summarizedDescriptions = "No description available for this item.";
                }
                System.out.println("Empty description summary");
            }

            if (! reviewSummarizer.isEmpty()) {
                summarizedReviews = reviewSummarizer.getSummary();
                System.out.println("Review summarization successful");
            } else {
                try {
                    summarizedReviews = info.getReviews().firstElement();
                } catch (NoSuchElementException nsee) {
                    summarizedReviews = "No reviews available for this item.";
                }
                System.out.println("Empty reviews summary");
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            summarizedDescriptions = info.getDescriptions().firstElement();
            summarizedReviews = info.getReviews().firstElement();
        }

        System.out.println("SUMMARISED DESCRIPTIONS");
        System.out.println(summarizedDescriptions);
        System.out.println("ORIGINAL DESCRIPTIONS");
        ppList(info.getDescriptions());

        System.out.println("SUMMARISED REVIEWS");
        System.out.println(summarizedReviews);
        System.out.println("ORIGINAL REVIEWS");
        ppList(info.getReviews());

        return new Summary(info, summarizedDescriptions, summarizedReviews);
    }


    /**
     * Serialise an object as a Base 64 string.
     *
     * @param o The object to serialise
     * @return Base64-encoded string.
     * @throws IOException
     */
    private static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.encodeBase64String(baos.toByteArray());
    }


    /**
     * Simple pretty print for lists
     *
     * @param l A List
     */
    public static void ppList(List<String> l) {
        Iterator itr = l.iterator();
        int i = 0;
        while (itr.hasNext()) {
            System.out.printf("[%d]\n%s\n", ++i, itr.next());
        }
    }


    public static void main(String[] args) {
        RESTBarcode test = new RESTBarcode();
        test.produceResponse("144932391X", "ISBN");
    }
}
