package com.android.swach.extra;

public class URL {
//    public static final String Url_IP = "103.74.54.55:7902";/////
    public static final String Url_IP = "103.74.54.30:7902";/////

//    Controller Name Admin
    public static String UserLogin = "http://" + Url_IP + "/api/Admin/UserLogin";
    public static String OtpGenerate = "http://" + Url_IP + "/api/Admin/OtpGenerate";
    public static String CheckOTP = "http://" + Url_IP + "/api/Admin/CheckOTP";

//    Controller Name SwachList
    public static String GetListPS = "http://" + Url_IP + "/api/SwachList/GetListPS";
    public static String GetListGP = "http://" + Url_IP + "/api/SwachList/GetListGP";
    public static String GetListVillage = "http://" + Url_IP + "/api/SwachList/GetListVillage";
    public static String GetListType = "http://" + Url_IP + "/api/SwachList/GetListType";
    public static String GetListCentre = "http://" + Url_IP + "/api/SwachList/GetListCentre";
    public static String GetListCenterMember = "http://" + Url_IP + "/api/SwachList/GetListCenterMember";
    public static String GetListStudent = "http://" + Url_IP + "/api/SwachList/GetListStudent";

    /// Controller Name DailyVisit
    public static String DailyVisitSave = "http://" + Url_IP + "/api/DailyVisit/DailyVisitSave";
    public static String DailyVisitSaveBulk = "http://" + Url_IP + "/api/DailyVisit/DailyVisitSaveBulk";

    /// Controller Name SwachData
    public static String GetCentreDetails = "http://" + Url_IP + "/api/SwachData/GetCentreDetails";


}


