package pt.unl.fct.di.apdc.flagnpatch.utils;

import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;

public class EmailUtil {

    private String html;

    public EmailUtil(String userKey, String userEmail, String userName, String randomId, EmailsEnum typeEmail) {
        if (typeEmail.equals(EmailsEnum.CONFIRMATION_EMAIL_HTML)) {
            this.html = setConfirmationHtml(userKey,userEmail, userName, randomId);
        } else
            this.html = setPasswordHtml(userKey,userEmail, userName, randomId);
    }

    public EmailUtil(String userName, EmailsEnum typeEmail, String county, String reportType) {
        this.html = setReportTypeChangeHtml(userName, typeEmail, county, reportType);
    }


    public String getHtml() {
        return html;
    }


    private String setConfirmationHtml(String userKey, String userEmail, String userName, String randomId) {

        String text = "Olá " + userName;

        String url = Utils.CONFIRMATION_EMAIL_LINK + "/" + userKey + "/" + randomId;
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> " +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" data-dnd=\"true\">\n" +
                "<head>\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1\" />\n" +
                "  <!--[if !mso]><!-->\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" />\n" +
                "  <!--<![endif]-->\n" +
                "\n" +
                "  <!--[if (gte mso 9)|(IE)]><style type=\"text/css\">\n" +
                "  table {border-collapse: collapse;}\n" +
                "  table, td {mso-table-lspace: 0pt;mso-table-rspace: 0pt;}\n" +
                "  img {-ms-interpolation-mode: bicubic;}\n" +
                "  </style>\n" +
                "  <![endif]-->\n" +
                "  <style type=\"text/css\">\n" +
                "  body {\n" +
                "    color: ;\n" +
                "  }\n" +
                "  body a {\n" +
                "    color: #0055B8;\n" +
                "    text-decoration: none;\n" +
                "  }\n" +
                "  p { margin: 0; padding: 0; }\n" +
                "  table[class=\"wrapper\"] {\n" +
                "    width:100% !important;\n" +
                "    table-layout: fixed;\n" +
                "    -webkit-font-smoothing: antialiased;\n" +
                "    -webkit-text-size-adjust: 100%;\n" +
                "    -moz-text-size-adjust: 100%;\n" +
                "    -ms-text-size-adjust: 100%;\n" +
                "  }\n" +
                "  img[class=\"max-width\"] {\n" +
                "    max-width: 100% !important;\n" +
                "  }\n" +
                "  @media screen and (max-width:480px) {\n" +
                "    .preheader .rightColumnContent,\n" +
                "    .footer .rightColumnContent {\n" +
                "        text-align: left !important;\n" +
                "    }\n" +
                "    .preheader .rightColumnContent div,\n" +
                "    .preheader .rightColumnContent span,\n" +
                "    .footer .rightColumnContent div,\n" +
                "    .footer .rightColumnContent span {\n" +
                "      text-align: left !important;\n" +
                "    }\n" +
                "    .preheader .rightColumnContent,\n" +
                "    .preheader .leftColumnContent {\n" +
                "      font-size: 80% !important;\n" +
                "      padding: 5px 0;\n" +
                "    }\n" +
                "    table[class=\"wrapper-mobile\"] {\n" +
                "      width: 100% !important;\n" +
                "      table-layout: fixed;\n" +
                "    }\n" +
                "    img[class=\"max-width\"] {\n" +
                "      height: auto !important;\n" +
                "    }\n" +
                "    a[class=\"bulletproof-button\"] {\n" +
                "      display: block !important;\n" +
                "      width: auto !important;\n" +
                "      font-size: 80%;\n" +
                "      padding-left: 0 !important;\n" +
                "      padding-right: 0 !important;\n" +
                "    }\n" +
                "    // 2 columns\n" +
                "    #templateColumns{\n" +
                "        width:100% !important;\n" +
                "    }\n" +
                "\n" +
                "    .templateColumnContainer{\n" +
                "        display:block !important;\n" +
                "        width:100% !important;\n" +
                "        padding-left: 0 !important;\n" +
                "        padding-right: 0 !important;\n" +
                "    }\n" +
                "  }\n" +
                "  </style>\n" +
                "  <style>\n" +
                "  body, p, div { font-family: trebuchet ms,sans-serif; }\n" +
                "</style>\n" +
                "  <style>\n" +
                "  body, p, div { font-size: 14px; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body yahoofix=\"true\" style=\"min-width: 100%; margin: 0; padding: 0; font-size: 14px; font-family: trebuchet ms,sans-serif; color: ; background-color: #F7F7F7; color: ;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22bodybackground%22%3A%22%23F7F7F7%22%2C%22bodyfontname%22%3A%22trebuchet%20ms%2Csans-serif%22%2C%22bodytextcolor%22%3A%22%22%2C%22bodylinkcolor%22%3A%22%230055B8%22%2C%22bodyfontsize%22%3A14%7D'>\n" +
                "  <center class=\"wrapper\">\n" +
                "    <div class=\"webkit\">\n" +
                "      <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" class=\"wrapper\" bgcolor=\"#F7F7F7\">\n" +
                "      <tr><td valign=\"top\" bgcolor=\"#F7F7F7\" width=\"100%\">\n" +
                "      <!--[if (gte mso 9)|(IE)]>\n" +
                "      <table width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "        <tr>\n" +
                "          <td>\n" +
                "          <![endif]-->\n" +
                "            <table width=\"100%\" role=\"content-container\" class=\"outer\" data-attributes='%7B%22dropped%22%3Atrue%2C%22containerpadding%22%3A%220%2C0%2C0%2C0%22%2C%22containerwidth%22%3A600%2C%22containerbackground%22%3A%22%23FFFFFF%22%7D' align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "              <tr>\n" +
                "                <td width=\"100%\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                  <tr>\n" +
                "                    <td>\n" +
                "                    <!--[if (gte mso 9)|(IE)]>\n" +
                "                      <table width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                        <tr>\n" +
                "                          <td>\n" +
                "                            <![endif]-->\n" +
                "                              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width: 100%; max-width:600px;\" align=\"center\">\n" +
                "                                <tr><td role=\"modules-container\" style=\"padding: 0px 0px 0px 0px; color: ; text-align: left;\" bgcolor=\"#FFFFFF\" width=\"100%\" align=\"left\">\n" +
                "                                  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" style=\"display:none !important; visibility:hidden; opacity:0; color:transparent; height:0; width:0;\" class=\"module preheader preheader-hide\" role=\"module\" data-type=\"preheader\">\n" +
                "  <tr><td role=\"module-content\"></td></tr>\n" +
                "</table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" role=\"module\" data-type=\"columns\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A1%2C%22padding%22%3A%225%2C0%2C0%2C0%22%2C%22cellpadding%22%3A0%2C%22containerbackground%22%3A%22%23f7f7f7%22%7D'>\n" +
                "  <tr><td style=\"padding: 5px 0px 0px 0px;\" bgcolor=\"#f7f7f7\">\n" +
                "    <table class=\"columns--container-table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        <td style=\"padding: 0px 0px 0px 0px\" role=\"column-0\" align=\"center\" valign=\"top\" width=\"100%\" height=\"100%\" class=\"templateColumnContainer column-drop-area \">\n" +
                "  <table role=\"module\" data-type=\"image\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" class=\"wrapper\" data-attributes='%7B%22child%22%3Afalse%2C%22link%22%3A%22%22%2C%22width%22%3A%2250%22%2C%22height%22%3A%2272%22%2C%22imagebackground%22%3A%22%23f7f7f7%22%2C%22url%22%3A%22https%3A//marketing-image-production.s3.amazonaws.com/uploads/30af0df2bd0c9968e78950e3c9f04510ebfdd7ea7dd68b6619129f780c99074ed1af912694a3c43f7fe064bd6d0f07d847646cc3e99765739f899f3172537a56.png%22%2C%22alt_text%22%3A%22%22%2C%22dropped%22%3Atrue%2C%22imagemargin%22%3A%2215%2C0%2C15%2C0%22%2C%22alignment%22%3A%22center%22%2C%22responsive%22%3Atrue%7D'>\n" +
                "<tr>\n" +
                "  <td style=\"font-size:6px;line-height:10px;background-color:#f7f7f7;padding: 15px 0px 15px 0px;\" valign=\"top\" align=\"center\" role=\"module-content\"><!--[if mso]>\n" +
                "<center>\n" +
                "<table width=\"50\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"table-layout: fixed;\">\n" +
                "  <tr>\n" +
                "    <td width=\"50\" valign=\"top\">\n" +
                "<![endif]-->\n" +
                "\n" +
                "  <img class=\"max-width\"  width=\"50\"   height=\"\"  src=\"https://marketing-image-production.s3.amazonaws.com/uploads/30af0df2bd0c9968e78950e3c9f04510ebfdd7ea7dd68b6619129f780c99074ed1af912694a3c43f7fe064bd6d0f07d847646cc3e99765739f899f3172537a56.png\" alt=\"\" border=\"0\" style=\"display: block; color: #000; text-decoration: none; font-family: Helvetica, arial, sans-serif; font-size: 16px;  max-width: 50px !important; width: 100% !important; height: auto !important; \" />\n" +
                "\n" +
                "<!--[if mso]>\n" +
                "</td></tr></table>\n" +
                "</center>\n" +
                "<![endif]--></td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%2230%2C20%2C20%2C20%22%2C%22containerbackground%22%3A%22%23d86b21%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 30px 20px 20px 20px;\" bgcolor=\"#d86b21\"><div style=\"text-align: center;\"><span style=\"font-size:28px;\"><span style=\"color:#FFFFFF;\">Confirmação de Email - Flag N' Patch</span></span></div>  <div style=\"text-align: center;\">&nbsp;</div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" role=\"module\" data-type=\"columns\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A1%2C%22padding%22%3A%2260%2C20%2C20%2C20%22%2C%22cellpadding%22%3A20%2C%22containerbackground%22%3A%22%22%7D'>\n" +
                "  <tr><td style=\"padding: 60px 20px 20px 20px;\" bgcolor=\"\">\n" +
                "    <table class=\"columns--container-table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        <td style=\"padding: 0px 20px 0px 20px\" role=\"column-0\" align=\"center\" valign=\"top\" width=\"100%\" height=\"100%\" class=\"templateColumnContainer column-drop-area \">\n" +
                "  <table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23ffffff%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#ffffff\"><div><span style=\"font-size: 22px;\">" + text + "</span></div>  <div>&nbsp;</div>  <div style=\"text-align: center;\"><span style=\"color: rgb(100, 100, 100);\">Para confirmar a sua conta e obter acesso a novas funcionalidades </span></div>  <div style=\"text-align: center;\"><span style=\"color: rgb(100, 100, 100);\">clique no botão abaixo.</span></div>  <div>&nbsp;</div>  <div style=\"text-align: right;\"><span style=\"font-size:28px;\"><span style=\"color: rgb(96, 96, 96);\">Flag N' Patch</span></span></div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "<table class=\"module\" role=\"module\" data-type=\"button\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22borderradius%22%3A10%2C%22buttonpadding%22%3A%2210%2C18%2C5%2C18%22%2C%22text%22%3A%22CLIQUE%2520AQUI%22%2C%22alignment%22%3A%22center%22%2C%22fontsize%22%3A16%2C%22url%22%3A%22www.google.pt%22%2C%22height%22%3A%22%22%2C%22width%22%3A450%2C%22containerbackground%22%3A%22%23ffffff%22%2C%22padding%22%3A%2250%2C10%2C0%2C0%22%2C%22buttoncolor%22%3A%22%23D86B21%22%2C%22textcolor%22%3A%22%23ffffff%22%2C%22bordercolor%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr>\n" +
                "  <td style=\"padding: 50px 10px 0px 0px;\" align=\"center\" bgcolor=\"#ffffff\">\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"wrapper-mobile\">\n" +
                "      <tr>\n" +
                "        <td align=\"center\" style=\"-webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; font-size: 16px;\" bgcolor=\"#D86B21\">\n" +
                "          <a href=" + url + " class=\"bulletproof-button\" target=\"_blank\" style=\"height: px; width: 450px; font-size: 16px; line-height: px; font-family: Helvetica, Arial, sans-serif; color: #ffffff; padding: 10px 18px 5px 18px; text-decoration: none; color: #ffffff; text-decoration: none; -webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; border: 1px solid #D86B21; display: inline-block;\">CLIQUE AQUI</a>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "\n" +
                "</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table><table class=\"module\" role=\"module\" data-type=\"spacer\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22spacing%22%3A2%2C%22containerbackground%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr><td role=\"module-content\" style=\"padding: 0px 0px 2px 0px;\" bgcolor=\"#D86B21\"></td></tr></table>\n" +
                "<table class=\"module\" role=\"module\" data-type=\"spacer\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22spacing%22%3A2%2C%22containerbackground%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr><td role=\"module-content\" style=\"padding: 0px 0px 2px 0px;\" bgcolor=\"#D86B21\"></td></tr></table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" class=\"module footer\" role=\"module\" data-type=\"footer\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A%222%22%2C%22padding%22%3A%2235%2C5%2C35%2C5%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "  <tr><td style=\"padding: 35px 5px 35px 5px;\" bgcolor=\"#F7F7F7\">\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        \n" +
                "        <td align=\"center\" valign=\"top\" width=\"50%\" height=\"100%\" class=\"templateColumnContainer\">\n" +
                "          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">\n" +
                "            <tr>\n" +
                "              <td class=\"leftColumnContent\" role=\"column-one\" height=\"100%\" style=\"height:100%;\"><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#F7F7F7\"><div style=\"font-size: 10px; line-height: 150%; margin: 0px;\">Flag N' Patch é uma empresa de prestação de serviços para os municípios de Portugal. Aceda à nossa página para mais informações.</div>  <div style=\"font-size: 10px; line-height: 150%; margin: 0px;\">&nbsp;</div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "            </tr>\n" +
                "          </table>\n" +
                "        </td>\n" +
                "        <td align=\"center\" valign=\"top\" width=\"50%\" height=\"100%\" class=\"templateColumnContainer\">\n" +
                "          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">\n" +
                "            <tr>\n" +
                "              <td class=\"rightColumnContent\" role=\"column-two\" height=\"100%\" style=\"height:100%;\"><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#F7F7F7\">" +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><span style=\"font-family:arial,helvetica,sans-serif;\"><span style=\"color:#7F7F7F;\">Flag N' Patch</span></span></div>  " +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><font color=\"#7f7f7f\" face=\"arial, helvetica, sans-serif\">FCT-UNL</font></div>  " +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><a href=\"https://www.facebook.com/FlagNPatch/\" target=\"_blank\">Visite-nos no Facebook</a></div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "            </tr>\n" +
                "          </table>\n" +
                "        </td>\n" +
                "        \n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table>\n" +
                "\n" +
                "                                </tr></td>\n" +
                "                              </table>\n" +
                "                            <!--[if (gte mso 9)|(IE)]>\n" +
                "                          </td>\n" +
                "                        </td>\n" +
                "                      </table>\n" +
                "                    <![endif]-->\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </table></td>\n" +
                "              </tr>\n" +
                "            </table>\n" +
                "          <!--[if (gte mso 9)|(IE)]>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "      <![endif]-->\n" +
                "      </tr></td>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "  </center>\n" +
                "</body>\n" +
                "</html>";
    }

    private String setReportTypeChangeHtml(String userName, EmailsEnum typeEmail, String county, String reportType) {

        String text = "Olá " + userName;
        String anotherText = null;

        if (typeEmail.equals(EmailsEnum.WORKER_ACTIVATED_HTML)) {
            anotherText = "<b>Parabéns</b>, a partir deste momento, está encarregue das ocorrências relacionadas com : <b>"
                    + reportType + "</b> no concelho <b>"+county+"</b>.<br>Visite a aplicação para começar a resolver as mesmas.";
        } else if (typeEmail.equals(EmailsEnum.WORKER_DEACTIVATE_HTML)) {
            anotherText = "<b>Infelizmente</b>, a partir deste momento, deixou de estar encarregue das ocorrências <b>Pendentes</b> relacionadas com :<b> "
                    + reportType + "</b> no concelho <b>"+county+"</b>.<br>Agradecemos o seu empenho por todas as ocorrências que resolveu e se encontra a resolver, e não deixe de visitar a plataforma para resolver outros tipos de ocorrências que tenha associado a si.";
        } else {
            anotherText = "Existiram alterações nos seus tipos de ocorrências, por favor visite a aplicação.";
        }

        String url = "https://flag-n-patch.appspot.com/login";

        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> " +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" data-dnd=\"true\">\n" +
                "<head>\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1\" />\n" +
                "  <!--[if !mso]><!-->\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" />\n" +
                "  <!--<![endif]-->\n" +
                "\n" +
                "  <!--[if (gte mso 9)|(IE)]><style type=\"text/css\">\n" +
                "  table {border-collapse: collapse;}\n" +
                "  table, td {mso-table-lspace: 0pt;mso-table-rspace: 0pt;}\n" +
                "  img {-ms-interpolation-mode: bicubic;}\n" +
                "  </style>\n" +
                "  <![endif]-->\n" +
                "  <style type=\"text/css\">\n" +
                "  body {\n" +
                "    color: ;\n" +
                "  }\n" +
                "  body a {\n" +
                "    color: #0055B8;\n" +
                "    text-decoration: none;\n" +
                "  }\n" +
                "  p { margin: 0; padding: 0; }\n" +
                "  table[class=\"wrapper\"] {\n" +
                "    width:100% !important;\n" +
                "    table-layout: fixed;\n" +
                "    -webkit-font-smoothing: antialiased;\n" +
                "    -webkit-text-size-adjust: 100%;\n" +
                "    -moz-text-size-adjust: 100%;\n" +
                "    -ms-text-size-adjust: 100%;\n" +
                "  }\n" +
                "  img[class=\"max-width\"] {\n" +
                "    max-width: 100% !important;\n" +
                "  }\n" +
                "  @media screen and (max-width:480px) {\n" +
                "    .preheader .rightColumnContent,\n" +
                "    .footer .rightColumnContent {\n" +
                "        text-align: left !important;\n" +
                "    }\n" +
                "    .preheader .rightColumnContent div,\n" +
                "    .preheader .rightColumnContent span,\n" +
                "    .footer .rightColumnContent div,\n" +
                "    .footer .rightColumnContent span {\n" +
                "      text-align: left !important;\n" +
                "    }\n" +
                "    .preheader .rightColumnContent,\n" +
                "    .preheader .leftColumnContent {\n" +
                "      font-size: 80% !important;\n" +
                "      padding: 5px 0;\n" +
                "    }\n" +
                "    table[class=\"wrapper-mobile\"] {\n" +
                "      width: 100% !important;\n" +
                "      table-layout: fixed;\n" +
                "    }\n" +
                "    img[class=\"max-width\"] {\n" +
                "      height: auto !important;\n" +
                "    }\n" +
                "    a[class=\"bulletproof-button\"] {\n" +
                "      display: block !important;\n" +
                "      width: auto !important;\n" +
                "      font-size: 80%;\n" +
                "      padding-left: 0 !important;\n" +
                "      padding-right: 0 !important;\n" +
                "    }\n" +
                "    // 2 columns\n" +
                "    #templateColumns{\n" +
                "        width:100% !important;\n" +
                "    }\n" +
                "\n" +
                "    .templateColumnContainer{\n" +
                "        display:block !important;\n" +
                "        width:100% !important;\n" +
                "        padding-left: 0 !important;\n" +
                "        padding-right: 0 !important;\n" +
                "    }\n" +
                "  }\n" +
                "  </style>\n" +
                "  <style>\n" +
                "  body, p, div { font-family: trebuchet ms,sans-serif; }\n" +
                "</style>\n" +
                "  <style>\n" +
                "  body, p, div { font-size: 14px; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body yahoofix=\"true\" style=\"min-width: 100%; margin: 0; padding: 0; font-size: 14px; font-family: trebuchet ms,sans-serif; color: ; background-color: #F7F7F7; color: ;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22bodybackground%22%3A%22%23F7F7F7%22%2C%22bodyfontname%22%3A%22trebuchet%20ms%2Csans-serif%22%2C%22bodytextcolor%22%3A%22%22%2C%22bodylinkcolor%22%3A%22%230055B8%22%2C%22bodyfontsize%22%3A14%7D'>\n" +
                "  <center class=\"wrapper\">\n" +
                "    <div class=\"webkit\">\n" +
                "      <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" class=\"wrapper\" bgcolor=\"#F7F7F7\">\n" +
                "      <tr><td valign=\"top\" bgcolor=\"#F7F7F7\" width=\"100%\">\n" +
                "      <!--[if (gte mso 9)|(IE)]>\n" +
                "      <table width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "        <tr>\n" +
                "          <td>\n" +
                "          <![endif]-->\n" +
                "            <table width=\"100%\" role=\"content-container\" class=\"outer\" data-attributes='%7B%22dropped%22%3Atrue%2C%22containerpadding%22%3A%220%2C0%2C0%2C0%22%2C%22containerwidth%22%3A600%2C%22containerbackground%22%3A%22%23FFFFFF%22%7D' align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "              <tr>\n" +
                "                <td width=\"100%\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                  <tr>\n" +
                "                    <td>\n" +
                "                    <!--[if (gte mso 9)|(IE)]>\n" +
                "                      <table width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                        <tr>\n" +
                "                          <td>\n" +
                "                            <![endif]-->\n" +
                "                              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width: 100%; max-width:600px;\" align=\"center\">\n" +
                "                                <tr><td role=\"modules-container\" style=\"padding: 0px 0px 0px 0px; color: ; text-align: left;\" bgcolor=\"#FFFFFF\" width=\"100%\" align=\"left\">\n" +
                "                                  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" style=\"display:none !important; visibility:hidden; opacity:0; color:transparent; height:0; width:0;\" class=\"module preheader preheader-hide\" role=\"module\" data-type=\"preheader\">\n" +
                "  <tr><td role=\"module-content\"></td></tr>\n" +
                "</table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" role=\"module\" data-type=\"columns\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A1%2C%22padding%22%3A%225%2C0%2C0%2C0%22%2C%22cellpadding%22%3A0%2C%22containerbackground%22%3A%22%23f7f7f7%22%7D'>\n" +
                "  <tr><td style=\"padding: 5px 0px 0px 0px;\" bgcolor=\"#f7f7f7\">\n" +
                "    <table class=\"columns--container-table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        <td style=\"padding: 0px 0px 0px 0px\" role=\"column-0\" align=\"center\" valign=\"top\" width=\"100%\" height=\"100%\" class=\"templateColumnContainer column-drop-area \">\n" +
                "  <table role=\"module\" data-type=\"image\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" class=\"wrapper\" data-attributes='%7B%22child%22%3Afalse%2C%22link%22%3A%22%22%2C%22width%22%3A%2250%22%2C%22height%22%3A%2272%22%2C%22imagebackground%22%3A%22%23f7f7f7%22%2C%22url%22%3A%22https%3A//marketing-image-production.s3.amazonaws.com/uploads/30af0df2bd0c9968e78950e3c9f04510ebfdd7ea7dd68b6619129f780c99074ed1af912694a3c43f7fe064bd6d0f07d847646cc3e99765739f899f3172537a56.png%22%2C%22alt_text%22%3A%22%22%2C%22dropped%22%3Atrue%2C%22imagemargin%22%3A%2215%2C0%2C15%2C0%22%2C%22alignment%22%3A%22center%22%2C%22responsive%22%3Atrue%7D'>\n" +
                "<tr>\n" +
                "  <td style=\"font-size:6px;line-height:10px;background-color:#f7f7f7;padding: 15px 0px 15px 0px;\" valign=\"top\" align=\"center\" role=\"module-content\"><!--[if mso]>\n" +
                "<center>\n" +
                "<table width=\"50\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"table-layout: fixed;\">\n" +
                "  <tr>\n" +
                "    <td width=\"50\" valign=\"top\">\n" +
                "<![endif]-->\n" +
                "\n" +
                "  <img class=\"max-width\"  width=\"50\"   height=\"\"  src=\"https://marketing-image-production.s3.amazonaws.com/uploads/30af0df2bd0c9968e78950e3c9f04510ebfdd7ea7dd68b6619129f780c99074ed1af912694a3c43f7fe064bd6d0f07d847646cc3e99765739f899f3172537a56.png\" alt=\"\" border=\"0\" style=\"display: block; color: #000; text-decoration: none; font-family: Helvetica, arial, sans-serif; font-size: 16px;  max-width: 50px !important; width: 100% !important; height: auto !important; \" />\n" +
                "\n" +
                "<!--[if mso]>\n" +
                "</td></tr></table>\n" +
                "</center>\n" +
                "<![endif]--></td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%2230%2C20%2C20%2C20%22%2C%22containerbackground%22%3A%22%23d86b21%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 30px 20px 20px 20px;\" bgcolor=\"#d86b21\"><div style=\"text-align: center;\"><span style=\"font-size:28px;\"><span style=\"color:#FFFFFF;\">Alterações na sua área - Flag N' Patch</span></span></div>  <div style=\"text-align: center;\">&nbsp;</div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" role=\"module\" data-type=\"columns\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A1%2C%22padding%22%3A%2260%2C20%2C20%2C20%22%2C%22cellpadding%22%3A20%2C%22containerbackground%22%3A%22%22%7D'>\n" +
                "  <tr><td style=\"padding: 60px 20px 20px 20px;\" bgcolor=\"\">\n" +
                "    <table class=\"columns--container-table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        <td style=\"padding: 0px 20px 0px 20px\" role=\"column-0\" align=\"center\" valign=\"top\" width=\"100%\" height=\"100%\" class=\"templateColumnContainer column-drop-area \">\n" +
                "  <table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23ffffff%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#ffffff\"><div><span style=\"font-size: 22px;\">" + text + "</span></div>  <div>&nbsp;</div>  <div style=\"text-align: center;\"><span style=\"color: rgb(100, 100, 100);\"> " + anotherText + " </span></div>  <div style=\"text-align: center;\"></div>  <div>&nbsp;</div>  <div style=\"text-align: right;\"><span style=\"font-size:28px;\"><span style=\"color: rgb(96, 96, 96);\">Flag N' Patch</span></span></div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "<table class=\"module\" role=\"module\" data-type=\"button\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22borderradius%22%3A10%2C%22buttonpadding%22%3A%2210%2C18%2C5%2C18%22%2C%22text%22%3A%22CLIQUE%2520AQUI%22%2C%22alignment%22%3A%22center%22%2C%22fontsize%22%3A16%2C%22url%22%3A%22www.google.pt%22%2C%22height%22%3A%22%22%2C%22width%22%3A450%2C%22containerbackground%22%3A%22%23ffffff%22%2C%22padding%22%3A%2250%2C10%2C0%2C0%22%2C%22buttoncolor%22%3A%22%23D86B21%22%2C%22textcolor%22%3A%22%23ffffff%22%2C%22bordercolor%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr>\n" +
                "  <td style=\"padding: 50px 10px 0px 0px;\" align=\"center\" bgcolor=\"#ffffff\">\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"wrapper-mobile\">\n" +
                "      <tr>\n" +
                "        <td align=\"center\" style=\"-webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; font-size: 16px;\" bgcolor=\"#D86B21\">\n" +
                "          <a href=" + url + " class=\"bulletproof-button\" target=\"_blank\" style=\"height: px; width: 450px; font-size: 16px; line-height: px; font-family: Helvetica, Arial, sans-serif; color: #ffffff; padding: 10px 18px 5px 18px; text-decoration: none; color: #ffffff; text-decoration: none; -webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; border: 1px solid #D86B21; display: inline-block;\">CLIQUE AQUI</a>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "\n" +
                "</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table><table class=\"module\" role=\"module\" data-type=\"spacer\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22spacing%22%3A2%2C%22containerbackground%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr><td role=\"module-content\" style=\"padding: 0px 0px 2px 0px;\" bgcolor=\"#D86B21\"></td></tr></table>\n" +
                "<table class=\"module\" role=\"module\" data-type=\"spacer\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22spacing%22%3A2%2C%22containerbackground%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr><td role=\"module-content\" style=\"padding: 0px 0px 2px 0px;\" bgcolor=\"#D86B21\"></td></tr></table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" class=\"module footer\" role=\"module\" data-type=\"footer\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A%222%22%2C%22padding%22%3A%2235%2C5%2C35%2C5%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "  <tr><td style=\"padding: 35px 5px 35px 5px;\" bgcolor=\"#F7F7F7\">\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        \n" +
                "        <td align=\"center\" valign=\"top\" width=\"50%\" height=\"100%\" class=\"templateColumnContainer\">\n" +
                "          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">\n" +
                "            <tr>\n" +
                "              <td class=\"leftColumnContent\" role=\"column-one\" height=\"100%\" style=\"height:100%;\"><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#F7F7F7\"><div style=\"font-size: 10px; line-height: 150%; margin: 0px;\">Flag N' Patch é uma empresa de prestação de serviços para os municípios de Portugal. Aceda à nossa página para mais informações.</div>  <div style=\"font-size: 10px; line-height: 150%; margin: 0px;\">&nbsp;</div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "            </tr>\n" +
                "          </table>\n" +
                "        </td>\n" +
                "        <td align=\"center\" valign=\"top\" width=\"50%\" height=\"100%\" class=\"templateColumnContainer\">\n" +
                "          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">\n" +
                "            <tr>\n" +
                "              <td class=\"rightColumnContent\" role=\"column-two\" height=\"100%\" style=\"height:100%;\"><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#F7F7F7\">" +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><span style=\"font-family:arial,helvetica,sans-serif;\"><span style=\"color:#7F7F7F;\">Flag N' Patch</span></span></div>  " +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><font color=\"#7f7f7f\" face=\"arial, helvetica, sans-serif\">FCT-UNL</font></div>  " +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><a href=\"https://www.facebook.com/FlagNPatch/\" target=\"_blank\">Visite-nos no Facebook</a></div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "            </tr>\n" +
                "          </table>\n" +
                "        </td>\n" +
                "        \n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table>\n" +
                "\n" +
                "                                </tr></td>\n" +
                "                              </table>\n" +
                "                            <!--[if (gte mso 9)|(IE)]>\n" +
                "                          </td>\n" +
                "                        </td>\n" +
                "                      </table>\n" +
                "                    <![endif]-->\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </table></td>\n" +
                "              </tr>\n" +
                "            </table>\n" +
                "          <!--[if (gte mso 9)|(IE)]>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "      <![endif]-->\n" +
                "      </tr></td>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "  </center>\n" +
                "</body>\n" +
                "</html>";
    }

    private String setPasswordHtml(String userKey, String userEmail, String username, String randomId) {
        String url = Utils.RECOVER_PASSWORD_LINK + "/" + userKey + "/" + randomId;
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> " +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" data-dnd=\"true\">\n" +
                "<head>\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1\" />\n" +
                "  <!--[if !mso]><!-->\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\" />\n" +
                "  <!--<![endif]-->\n" +
                "\n" +
                "  <!--[if (gte mso 9)|(IE)]><style type=\"text/css\">\n" +
                "  table {border-collapse: collapse;}\n" +
                "  table, td {mso-table-lspace: 0pt;mso-table-rspace: 0pt;}\n" +
                "  img {-ms-interpolation-mode: bicubic;}\n" +
                "  </style>\n" +
                "  <![endif]-->\n" +
                "  <style type=\"text/css\">\n" +
                "  body {\n" +
                "    color: ;\n" +
                "  }\n" +
                "  body a {\n" +
                "    color: #0055B8;\n" +
                "    text-decoration: none;\n" +
                "  }\n" +
                "  p { margin: 0; padding: 0; }\n" +
                "  table[class=\"wrapper\"] {\n" +
                "    width:100% !important;\n" +
                "    table-layout: fixed;\n" +
                "    -webkit-font-smoothing: antialiased;\n" +
                "    -webkit-text-size-adjust: 100%;\n" +
                "    -moz-text-size-adjust: 100%;\n" +
                "    -ms-text-size-adjust: 100%;\n" +
                "  }\n" +
                "  img[class=\"max-width\"] {\n" +
                "    max-width: 100% !important;\n" +
                "  }\n" +
                "  @media screen and (max-width:480px) {\n" +
                "    .preheader .rightColumnContent,\n" +
                "    .footer .rightColumnContent {\n" +
                "        text-align: left !important;\n" +
                "    }\n" +
                "    .preheader .rightColumnContent div,\n" +
                "    .preheader .rightColumnContent span,\n" +
                "    .footer .rightColumnContent div,\n" +
                "    .footer .rightColumnContent span {\n" +
                "      text-align: left !important;\n" +
                "    }\n" +
                "    .preheader .rightColumnContent,\n" +
                "    .preheader .leftColumnContent {\n" +
                "      font-size: 80% !important;\n" +
                "      padding: 5px 0;\n" +
                "    }\n" +
                "    table[class=\"wrapper-mobile\"] {\n" +
                "      width: 100% !important;\n" +
                "      table-layout: fixed;\n" +
                "    }\n" +
                "    img[class=\"max-width\"] {\n" +
                "      height: auto !important;\n" +
                "    }\n" +
                "    a[class=\"bulletproof-button\"] {\n" +
                "      display: block !important;\n" +
                "      width: auto !important;\n" +
                "      font-size: 80%;\n" +
                "      padding-left: 0 !important;\n" +
                "      padding-right: 0 !important;\n" +
                "    }\n" +
                "    // 2 columns\n" +
                "    #templateColumns{\n" +
                "        width:100% !important;\n" +
                "    }\n" +
                "\n" +
                "    .templateColumnContainer{\n" +
                "        display:block !important;\n" +
                "        width:100% !important;\n" +
                "        padding-left: 0 !important;\n" +
                "        padding-right: 0 !important;\n" +
                "    }\n" +
                "  }\n" +
                "  </style>\n" +
                "  <style>\n" +
                "  body, p, div { font-family: trebuchet ms,sans-serif; }\n" +
                "</style>\n" +
                "  <style>\n" +
                "  body, p, div { font-size: 14px; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body yahoofix=\"true\" style=\"min-width: 100%; margin: 0; padding: 0; font-size: 14px; font-family: trebuchet ms,sans-serif; color: ; background-color: #F7F7F7; color: ;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22bodybackground%22%3A%22%23F7F7F7%22%2C%22bodyfontname%22%3A%22trebuchet%20ms%2Csans-serif%22%2C%22bodytextcolor%22%3A%22%22%2C%22bodylinkcolor%22%3A%22%230055B8%22%2C%22bodyfontsize%22%3A14%7D'>\n" +
                "  <center class=\"wrapper\">\n" +
                "    <div class=\"webkit\">\n" +
                "      <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" class=\"wrapper\" bgcolor=\"#F7F7F7\">\n" +
                "      <tr><td valign=\"top\" bgcolor=\"#F7F7F7\" width=\"100%\">\n" +
                "      <!--[if (gte mso 9)|(IE)]>\n" +
                "      <table width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "        <tr>\n" +
                "          <td>\n" +
                "          <![endif]-->\n" +
                "            <table width=\"100%\" role=\"content-container\" class=\"outer\" data-attributes='%7B%22dropped%22%3Atrue%2C%22containerpadding%22%3A%220%2C0%2C0%2C0%22%2C%22containerwidth%22%3A600%2C%22containerbackground%22%3A%22%23FFFFFF%22%7D' align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "              <tr>\n" +
                "                <td width=\"100%\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                  <tr>\n" +
                "                    <td>\n" +
                "                    <!--[if (gte mso 9)|(IE)]>\n" +
                "                      <table width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "                        <tr>\n" +
                "                          <td>\n" +
                "                            <![endif]-->\n" +
                "                              <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width: 100%; max-width:600px;\" align=\"center\">\n" +
                "                                <tr><td role=\"modules-container\" style=\"padding: 0px 0px 0px 0px; color: ; text-align: left;\" bgcolor=\"#FFFFFF\" width=\"100%\" align=\"left\">\n" +
                "                                  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" style=\"display:none !important; visibility:hidden; opacity:0; color:transparent; height:0; width:0;\" class=\"module preheader preheader-hide\" role=\"module\" data-type=\"preheader\">\n" +
                "  <tr><td role=\"module-content\"></td></tr>\n" +
                "</table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" role=\"module\" data-type=\"columns\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A1%2C%22padding%22%3A%225%2C0%2C0%2C0%22%2C%22cellpadding%22%3A0%2C%22containerbackground%22%3A%22%23f7f7f7%22%7D'>\n" +
                "  <tr><td style=\"padding: 5px 0px 0px 0px;\" bgcolor=\"#f7f7f7\">\n" +
                "    <table class=\"columns--container-table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        <td style=\"padding: 0px 0px 0px 0px\" role=\"column-0\" align=\"center\" valign=\"top\" width=\"100%\" height=\"100%\" class=\"templateColumnContainer column-drop-area \">\n" +
                "  <table role=\"module\" data-type=\"image\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" class=\"wrapper\" data-attributes='%7B%22child%22%3Afalse%2C%22link%22%3A%22%22%2C%22width%22%3A%2250%22%2C%22height%22%3A%2272%22%2C%22imagebackground%22%3A%22%23f7f7f7%22%2C%22url%22%3A%22https%3A//marketing-image-production.s3.amazonaws.com/uploads/30af0df2bd0c9968e78950e3c9f04510ebfdd7ea7dd68b6619129f780c99074ed1af912694a3c43f7fe064bd6d0f07d847646cc3e99765739f899f3172537a56.png%22%2C%22alt_text%22%3A%22%22%2C%22dropped%22%3Atrue%2C%22imagemargin%22%3A%2215%2C0%2C15%2C0%22%2C%22alignment%22%3A%22center%22%2C%22responsive%22%3Atrue%7D'>\n" +
                "<tr>\n" +
                "  <td style=\"font-size:6px;line-height:10px;background-color:#f7f7f7;padding: 15px 0px 15px 0px;\" valign=\"top\" align=\"center\" role=\"module-content\"><!--[if mso]>\n" +
                "<center>\n" +
                "<table width=\"50\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"table-layout: fixed;\">\n" +
                "  <tr>\n" +
                "    <td width=\"50\" valign=\"top\">\n" +
                "<![endif]-->\n" +
                "\n" +
                "  <img class=\"max-width\"  width=\"50\"   height=\"\"  src=\"https://marketing-image-production.s3.amazonaws.com/uploads/30af0df2bd0c9968e78950e3c9f04510ebfdd7ea7dd68b6619129f780c99074ed1af912694a3c43f7fe064bd6d0f07d847646cc3e99765739f899f3172537a56.png\" alt=\"\" border=\"0\" style=\"display: block; color: #000; text-decoration: none; font-family: Helvetica, arial, sans-serif; font-size: 16px;  max-width: 50px !important; width: 100% !important; height: auto !important; \" />\n" +
                "\n" +
                "<!--[if mso]>\n" +
                "</td></tr></table>\n" +
                "</center>\n" +
                "<![endif]--></td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%2230%2C20%2C20%2C20%22%2C%22containerbackground%22%3A%22%23d86b21%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 30px 20px 20px 20px;\" bgcolor=\"#d86b21\"><div style=\"text-align: center;\"><span style=\"font-size:28px;\"><span style=\"color:#FFFFFF;\">Recuperação de Password - Flag N' Patch</span></span></div>  <div style=\"text-align: center;\">&nbsp;</div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" role=\"module\" data-type=\"columns\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A1%2C%22padding%22%3A%2260%2C20%2C20%2C20%22%2C%22cellpadding%22%3A20%2C%22containerbackground%22%3A%22%22%7D'>\n" +
                "  <tr><td style=\"padding: 60px 20px 20px 20px;\" bgcolor=\"\">\n" +
                "    <table class=\"columns--container-table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        <td style=\"padding: 0px 20px 0px 20px\" role=\"column-0\" align=\"center\" valign=\"top\" width=\"100%\" height=\"100%\" class=\"templateColumnContainer column-drop-area \">\n" +
                "  <table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23ffffff%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#ffffff\"><div><span style=\"font-size: 22px;\">Olá " + username + "</span></div>  <div>&nbsp;</div>  <div style=\"text-align: center;\"><span style=\"color: rgb(100, 100, 100);\">Para alterar a sua password de acesso </span></div>  <div style=\"text-align: center;\"><span style=\"color: rgb(100, 100, 100);\">clique no botão abaixo.</span></div>  <div>&nbsp;</div>  <div style=\"text-align: right;\"><span style=\"font-size:28px;\"><span style=\"color: rgb(96, 96, 96);\">Flag N' Patch</span></span></div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "<table class=\"module\" role=\"module\" data-type=\"button\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22borderradius%22%3A10%2C%22buttonpadding%22%3A%2210%2C18%2C5%2C18%22%2C%22text%22%3A%22CLIQUE%2520AQUI%22%2C%22alignment%22%3A%22center%22%2C%22fontsize%22%3A16%2C%22url%22%3A%22www.google.pt%22%2C%22height%22%3A%22%22%2C%22width%22%3A450%2C%22containerbackground%22%3A%22%23ffffff%22%2C%22padding%22%3A%2250%2C10%2C0%2C0%22%2C%22buttoncolor%22%3A%22%23D86B21%22%2C%22textcolor%22%3A%22%23ffffff%22%2C%22bordercolor%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr>\n" +
                "  <td style=\"padding: 50px 10px 0px 0px;\" align=\"center\" bgcolor=\"#ffffff\">\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"wrapper-mobile\">\n" +
                "      <tr>\n" +
                "        <td align=\"center\" style=\"-webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; font-size: 16px;\" bgcolor=\"#D86B21\">\n" +
                "          <a href=" + url + " class=\"bulletproof-button\" target=\"_blank\" style=\"height: px; width: 450px; font-size: 16px; line-height: px; font-family: Helvetica, Arial, sans-serif; color: #ffffff; padding: 10px 18px 5px 18px; text-decoration: none; color: #ffffff; text-decoration: none; -webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px; border: 1px solid #D86B21; display: inline-block;\">CLIQUE AQUI</a>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "\n" +
                "</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table><table class=\"module\" role=\"module\" data-type=\"spacer\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22spacing%22%3A2%2C%22containerbackground%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr><td role=\"module-content\" style=\"padding: 0px 0px 2px 0px;\" bgcolor=\"#D86B21\"></td></tr></table>\n" +
                "<table class=\"module\" role=\"module\" data-type=\"spacer\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22spacing%22%3A2%2C%22containerbackground%22%3A%22%23D86B21%22%7D'>\n" +
                "<tr><td role=\"module-content\" style=\"padding: 0px 0px 2px 0px;\" bgcolor=\"#D86B21\"></td></tr></table>\n" +
                "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\" class=\"module footer\" role=\"module\" data-type=\"footer\" data-attributes='%7B%22dropped%22%3Atrue%2C%22columns%22%3A%222%22%2C%22padding%22%3A%2235%2C5%2C35%2C5%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "  <tr><td style=\"padding: 35px 5px 35px 5px;\" bgcolor=\"#F7F7F7\">\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" width=\"100%\">\n" +
                "      <tr role=\"module-content\">\n" +
                "        \n" +
                "        <td align=\"center\" valign=\"top\" width=\"50%\" height=\"100%\" class=\"templateColumnContainer\">\n" +
                "          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">\n" +
                "            <tr>\n" +
                "              <td class=\"leftColumnContent\" role=\"column-one\" height=\"100%\" style=\"height:100%;\"><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#F7F7F7\"><div style=\"font-size: 10px; line-height: 150%; margin: 0px;\">Flag N' Patch é uma empresa de prestação de serviços para os municípios de Portugal. Aceda à nossa página para mais informações.</div>  <div style=\"font-size: 10px; line-height: 150%; margin: 0px;\">&nbsp;</div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "            </tr>\n" +
                "          </table>\n" +
                "        </td>\n" +
                "        <td align=\"center\" valign=\"top\" width=\"50%\" height=\"100%\" class=\"templateColumnContainer\">\n" +
                "          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">\n" +
                "            <tr>\n" +
                "              <td class=\"rightColumnContent\" role=\"column-two\" height=\"100%\" style=\"height:100%;\"><table class=\"module\" role=\"module\" data-type=\"text\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"  width=\"100%\" style=\"table-layout: fixed;\" data-attributes='%7B%22dropped%22%3Atrue%2C%22child%22%3Afalse%2C%22padding%22%3A%220%2C0%2C0%2C0%22%2C%22containerbackground%22%3A%22%23F7F7F7%22%7D'>\n" +
                "<tr>\n" +
                "  <td role=\"module-content\"  valign=\"top\" height=\"100%\" style=\"padding: 0px 0px 0px 0px;\" bgcolor=\"#F7F7F7\">" +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><span style=\"font-family:arial,helvetica,sans-serif;\"><span style=\"color:#7F7F7F;\">Flag N' Patch</span></span></div>  " +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><font color=\"#7f7f7f\" face=\"arial, helvetica, sans-serif\">FCT-UNL</font></div>  " +
                "<div style=\"font-size: 10px; line-height: 150%; margin: 0px; text-align: right;\"><a href=\"https://www.facebook.com/FlagNPatch/\" target=\"_blank\">Visite-nos no Facebook</a></div> </td>\n" +
                "</tr>\n" +
                "</table>\n" +
                "</td>\n" +
                "            </tr>\n" +
                "          </table>\n" +
                "        </td>\n" +
                "        \n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </td></tr>\n" +
                "</table>\n" +
                "\n" +
                "                                </tr></td>\n" +
                "                              </table>\n" +
                "                            <!--[if (gte mso 9)|(IE)]>\n" +
                "                          </td>\n" +
                "                        </td>\n" +
                "                      </table>\n" +
                "                    <![endif]-->\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </table></td>\n" +
                "              </tr>\n" +
                "            </table>\n" +
                "          <!--[if (gte mso 9)|(IE)]>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </table>\n" +
                "      <![endif]-->\n" +
                "      </tr></td>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "  </center>\n" +
                "</body>\n" +
                "</html>";
    }
}
