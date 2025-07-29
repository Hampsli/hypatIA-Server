package com.hypatia.Constants;

public class Queries {

    public static final String QUERY_GET_AI_INTERACTIONS_BY_USER_ID="select * from ai_interactions aiInt where aiInt.user_id=:userID order by aiInt.created_at desc limit 1";
}
