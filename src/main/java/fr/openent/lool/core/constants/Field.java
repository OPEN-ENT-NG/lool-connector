package fr.openent.lool.core.constants;

public class Field {

    private Field() {
        throw new IllegalStateException("Utility class");
    }

    public static final String METADATA = "metadata";
    public static final String ID = "id";
    public static final String _ID = "_id";
    public static final String DOCUMENTID = "documentId";
    public static final String DOCUMENTS = "documents";
    public static final String $SET = "$set";
    public static final String FILE = "file";
    public static final String BASEFILENAME = "BaseFileName";
    public static final String NAME = "name";
    public static final String SIZE = "Size";
    public static final String size = "size";
    public static final String OWNERID = "OwnerId";
    public static final String OWNER = "owner";
    public static final String USERID = "UserId";
    public static final String USERFRIENDLYNAME = "UserFriendlyName";
    public static final String VERSION = "Version";
    public static final String LASTMODIFIEDTIME = "LastModifiedTime";
    public static final String MODIFIED = "modified";
    public static final String FILE_DATE = "fileDate";
    public static final String DATE = "date";
    public static final String USERCANWRITE = "UserCanWrite";
    public static final String TOKEN = "token";
    public static final String DOCUMENTSREVISION = "documentsRevisions";
    public static final String STATUS = "status";
    public static final String OK = "ok";
    public static final String POSTMESSAGEORIGIN = "PostMessageOrigin";

    //OnlyOffice extension
    public static final String PPTX = "pptx";
    public static final String XLSX = "xlsx";
    public static final String DOCX = "docx";

    //LibreOffice extension
    public static final String ODP = "odp";
    public static final String ODT = "odt";
    public static final String ODS = "ods";
    public static final String ODG = "odg";


}