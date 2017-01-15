package xyz.donit.domain.goal;

import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.util.HashMap;

/**
 * Created by felix on 1/12/17.
 */
public class DaySet extends HashMap<Integer, Boolean> {
    private static final int MASK = 0x10F447;

    private void parseInt(int days) {
        days &= MASK;
        for (int i = 0; i < 7; i ++){
            if((days & (1 << i)) !=0){
                this.put(i, true);
            }else{
                this.put(i, false);
            }
        }
    }

    public int toInt(){
        int days = 0;
        for (int i = 0; i < 7; i++){
            if(this.get(i) == true){
                days |= (1 << i);
            }
        }
        return days & MASK;
    }

    public DaySet(){
        for(int i = 0; i < 7; i ++){
            this.put(i, false);
        }
    }

    public DaySet(int days){
        super();
        this.parseInt(days);
    }

}
