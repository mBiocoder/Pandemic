package simulation;

import java.util.ArrayList;
import java.util.Collection;

public class Human {
    ArrayList<Human> friends;
    int num_friends;
    boolean alive;
    boolean infected;
    int infectedWeeksLeft;
    boolean recovered;

    /**
     * @param num_friends number of other humans that the human will be
     *                  in contact with each week, and that could get infected
     */
    public Human(int num_friends){
        this.num_friends=num_friends;
        alive=true;
        infected=false;
        friends=new ArrayList<>();
    }

    /**
     * adds persons that this Human knows and can have contact with
     * @param friends persons that the Human knows
     */
    public void addFrieds(Collection<Human> friends) {
        this.friends.addAll(friends);
    }

    /**
     * changes the status to infected and sets the time the Human will stay infected
     * @param d Disease with which the Human gets infected
     */
    public void infect(Disease d)
    {
        if(recovered || !alive)
            return;
        infected=true;
        infectedWeeksLeft=d.duration;
    }

    /**
     * updates the infected status after another week has passed, if the duration of
     * the infection is over the status is set to recovered
     */
    public void updateInfection()
    {
        if(!infected)
            return;
        infectedWeeksLeft--;
        if(infectedWeeksLeft==0) {
            infected = false;
            recovered = true;
        }
    }
}

