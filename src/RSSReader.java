import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Put your name here
 *
 */
public final class RSSReader {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSReader() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title </title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";
        //Writing the initial tags and title.
        out.println("<html>");
        out.println("<head>");
        out.println("<title>");

        //Finding what's the index of the title tags using the getChildElement method.
        int indexTit = getChildElement(channel, "title");
        int indexDesc = getChildElement(channel, "description");
        int indexLink = getChildElement(channel, "link");

        //Creating title, description and link variables.
        XMLTree title = channel.child(indexTit);
        String titleText = "";
        if (title.numberOfChildren() > 0) {
            titleText = title.child(0).label();
        }
        XMLTree description = channel.child(indexDesc);
        String descText = "";
        if (description.numberOfChildren() > 0) {
            descText = description.child(0).label();
        }
        XMLTree link = channel.child(indexLink);
        String linkURL = link.child(0).label();

        //Printing title to file and closing some tags
        out.println(titleText);
        out.println("</title>");
        out.println("</head>");
        out.println("<body>");

        //Printing rest of the file
        out.println(
                " <h1><a href=\"" + linkURL + "\">" + titleText + "</a></h1>");
        out.println(" <p>" + descText + "</p>");
        out.println(" <table border=\"1\">");
        out.println("  <tr>");
        out.println("   <th>Date</th>");
        out.println("   <th>Source</th>");
        out.println("   <th>News</th>");
        out.println("  </tr>");
    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.println(" </table>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        //Creating variables used in loop
        int index = -1;
        int len = xml.numberOfChildren();

        //Loop to find the given tag
        for (int i = 0; i < len; i++) {
            //Getting the ith child of the given tree
            XMLTree a = xml.child(i);
            //Checking if the label of the child matches the given tag needed to be found
            if (a.label().equals(tag)) {
                //If true, set index to current value of i
                index = i;
            }
        }

        //return index
        return index;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        //Printing initial header
        out.println("  <tr>");

        //Getting the index of the item children (if they are there);
        int indexDate = getChildElement(item, "pubDate");
        int indexTitle = getChildElement(item, "title");
        int indexDesc = -1;
        if (indexTitle == -1) {
            indexDesc = getChildElement(item, "description");
        }
        int indexSource = getChildElement(item, "source");
        int indexLink = getChildElement(item, "link");

        //Printing date if available
        if (indexDate >= 0) {
            String date = item.child(indexDate).child(0).label();
            out.println("   <td>" + date + "</td>");
        } else {
            out.println("   <td>No date available</td>");
        }

        //Printing source if available
        if (indexSource >= 0) {
            String source = item.child(indexSource).child(0).label();
            String sourceURL = item.child(indexSource).attributeValue("url");
            out.println("   <td><a href=\"" + sourceURL + "\">" + source
                    + "</a></td>");
        } else {
            out.println("   <td>No source available</td>");
        }

        //Printing title + link if available
        String link = "";
        if (indexLink >= 0) {
            link = item.child(indexLink).child(0).label();
        }
        if (indexTitle >= 0
                && (item.child(indexTitle).numberOfChildren() > 0)) {
            String title = item.child(indexTitle).child(0).label();
            if (!link.equals("")) {
                out.println("   <td><a href=\"" + link + "\">" + title
                        + "</a></td>");
            } else {
                out.println("   <td>" + title + "</td>");
            }
        } else if (indexDesc >= 0
                && (item.child(indexDesc).numberOfChildren() > 0)) {
            String description = item.child(indexDesc).child(0).label();
            if (!link.equals("")) {
                out.println("   <td><a href=\"" + link + "\">" + description
                        + "</a></td>");
            } else {
                out.println("   <td>" + description + "</td>");
            }
        } else {
            out.println("   <td>No title/description available");
        }

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        //Asking user for a URL and assign it to a XML variable.
        out.print("Please enter a valid URL of a RSS 2.0 feed: ");
        String url = in.nextLine();
        XMLTree root = new XMLTree1(url);

        //Checking to see if the URL is a valid RSS 2.0 feed.
        if (root.label().equals("rss")
                && root.attributeValue("version").equals("2.0")) {
            //If so, ask user for the output file.
            out.print("Please enter a HTML file to serve as output: ");
            String userFile = in.nextLine();

            //Getting the channel, which will be the "real" root.
            XMLTree channel = root.child(0);

            //Creating an output stream for the file.
            SimpleWriter fileOut = new SimpleWriter1L(userFile);

            //Writing the opening tags of the file, using the method outputHeader.
            outputHeader(channel, fileOut);

            //Getting the total number of children in the channel tag
            int children = channel.numberOfChildren();
            //Loop to get all the items
            for (int i = 0; i < children; i++) {
                XMLTree curr = channel.child(i);
                //If the current children is an item tag, process it.
                if (curr.label().equals("item")) {
                    processItem(curr, fileOut);
                }
            }

            //Writing the closing tags of the file, using the method outputFooter
            outputFooter(fileOut);
            fileOut.close();
        } else {
            out.println("Your file was not found to be a valid RSS 2.0 feed.");
            out.println("Please restart the program and try again.");
        }

        in.close();
        out.close();
    }

}
