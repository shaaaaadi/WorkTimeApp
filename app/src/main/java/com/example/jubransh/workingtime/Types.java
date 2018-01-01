package com.example.jubransh.workingtime;

import java.sql.Struct;

/**
 * Created by jubransh on 11/18/2016.
 */

public final class Types
{
    public enum DB_ERROR
                        {
                            NO_ERROR,
                            CORRUPTED_FILE,
                            DB_FILE_NOT_FOUND,
                            ILLEGAL_FILE_FORMAT
                        };
    public enum BUTTON_TYPE {START_BUTTON, STOP_BUTTON};
    public  enum GENGER {MALE, FEMALE};

}
