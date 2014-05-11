package web;

/** A Response subclass that adds a HTML header and footer. */
class TemplateResponse extends Response {
    TemplateResponse() {
    }

    TemplateResponse(String s) {
        super(s);
    }

    /** Adds the HTML header to the given StringBuilder. */
    protected static void buildHead(StringBuilder resp) {
        resp.append("<html>")
                .append("  <head>")
                .append("    <title>SerialKiller</title>")
                .append("    <meta charset='utf-8'>")
                .append("    <link rel=\"icon\" href=\"data:;base64,=\">")
                .append("    <style>")
                .append("      body { font-family: sans-serif; font-size: small; color: #333; }")
                .append("      a { color: #2980b9; text-decoration: none; }")
                .append("      table { font-size: small; }")
                .append("    </style>")
                .append("  </head>")
                .append("  <body>")
                .append("    <a href='/'>Home</a> ")
                .append("    <a href='/chat/'>Chat</a> ")
                .append("    <a href='/files/'>Files</a> ")
                .append("    <a href='/logs/'>Logs</a> ")
                .append("    <a href='/stats/'>Stats</a> ")
                .append("    <a href='' style='float: right'>Refresh</a>")
                .append("    <hr>");
    }

    /** Adds the HTML footer to the given StringBuilder. */
    protected static void buildFoot(StringBuilder resp) {
        resp.append("</body></html>");
    }

    @Override
    protected void buildContent(StringBuilder resp) {
        contentType = ContentType.textHtml;

        buildHead(resp);
        super.buildContent(resp);
        buildFoot(resp);
    }
}