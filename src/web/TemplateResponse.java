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
                .append("      body { font-family: sans-serif; font-size: small; color: #333; margin: 0; }")
                .append("      a { color: #2980b9; text-decoration: none; }")
                .append("      #nav { background-color: #2980b9; padding: 10px; }")
                .append("      #nav a { color: white; }")
                .append("      #content { padding: 10px; }")
                .append("      table { font-size: small; }")
                .append("    </style>")
                .append("  </head>")
                .append("  <body>")
                .append("    <div id='nav'>")
                .append("    <a href='/'>Home</a>&nbsp; ")
                .append("    <a href='/chat/'>Chat</a>&nbsp; ")
                .append("    <a href='/files/'>Files</a>&nbsp; ")
                .append("    <a href='/logs/'>Logs</a>&nbsp; ")
                .append("    <a href='/status/'>Status</a>&nbsp; ")
                .append("    <a href='' style='float: right'>Refresh</a>")
                .append("    </div>")
                .append("    <div id='content'>");
    }

    /** Adds the HTML footer to the given StringBuilder. */
    protected static void buildFoot(StringBuilder resp) {
        resp.append("</div></body></html>");
    }

    @Override
    protected void buildContent(StringBuilder resp) {
        contentType = ContentType.textHtml;

        buildHead(resp);
        super.buildContent(resp);
        buildFoot(resp);
    }
}