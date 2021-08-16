package simulation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Pandemic {
    ArrayList<Human> population;
    Disease d;
    ArrayList<Human> currentlyInfected;
    ArrayList<Human> recovered;
    ArrayList<Human> dead;
    private ChartUtils ChartUtilities;

    public Pandemic(int populationSize, Disease d)
    {
        this.d=d;
        population = new ArrayList<>();
        currentlyInfected = new ArrayList<>();
        recovered = new ArrayList<>();
        dead = new ArrayList<>();


        int friendsRange = Config.MAX_FRIENDS-Config.MIN_FRIENDS;
        for(int i=0; i<populationSize; i++){
            int friends = (int)(Math.random()*friendsRange)+Config.MIN_FRIENDS;
            population.add(new Human(friends));
        }
        //add friends to humans
        for(Human h : population){
            h.addFrieds(Pandemic.getRandomSubset(population,h.num_friends));
        }
        //initialize infections
        for(Human selected : Pandemic.getRandomSubset(population,Config.INITIALLY_INFECTED_HUMANS))
        {
            selected.infect(d);
            currentlyInfected.add(selected);
        }
    }

    /** select a random subset of a given size from a given set
     * @param set List of human from which the subset is sampled
     * @param size size of the sampled subset
     * @return random subset of set with size size
     */
    private static HashSet<Human> getRandomSubset(List<Human> set, int size){
        HashSet<Human> subset = new HashSet<>();
        while(subset.size()<size){
            subset.add(set.get((int)(Math.random()*set.size())));
        }
        return subset;
    }

    /** simulates the changes of infected/recovered/dead people in the population
     */
    public  void simulateWeek(){
        //infected will infect others
        ArrayList<Human> newlyInfected = new ArrayList<>();
        for(Human h : currentlyInfected){
            for(Human f : Pandemic.getRandomSubset(h.friends,d.num_infected)){
                int num_contacts = (int)(h.num_friends*Config.CONTACT_TO_FRIENDS_RATIO);
                int num_infected = (int)Math.floor(d.infection_rate * num_contacts);
                if(f.infected)
                    continue;
                f.infect(d);
                if(f.infected)
                    newlyInfected.add(f);
            }
        }
        //some infected will die
        ArrayList <Human> newlyDead = new ArrayList<>();
        for(Human h : currentlyInfected){
            double r = Math.random();
            if(r>d.mortality_rate){
                h.alive=false;
                dead.add(h);
                currentlyInfected.remove(h);
                population.remove(h);
            }
        }
        //some infected will recover
        ArrayList<Human> newlyRecovered = new ArrayList<>();
        for(Human h : currentlyInfected)
        {
            h.updateInfection();
            if(h.recovered)
                newlyRecovered.add(h);
        }
        recovered.addAll(newlyRecovered);
        currentlyInfected.removeAll(newlyRecovered);
        currentlyInfected.addAll(newlyInfected);
    }

    /**
     * simulates the developments of dead/infected/recovered cases over multiple weeks and creates a plot
     * @param weeks number of weeks that should be simulated
     * @param image file to which the plot should be saved
     */
    public void simulate(int weeks, File image) {
        //plotting of number of dead/recovered/infected over time
        XYSeries deadLine = new XYSeries("dead");
        XYSeries recoveredLine = new XYSeries("recovered");
        XYSeries infectedLine = new XYSeries("infected");
        deadLine.add(0,this.dead.size());
        recoveredLine.add(0,this.recovered.size());
        infectedLine.add(0,this.currentlyInfected.size());
        System.out.println("#dead\tinfected\trecovered");
        System.out.println(this.dead.size() + "\t" + currentlyInfected.size() + "\t" + recovered.size());
        //simulation of the developments each week and update of plot
        for (int w = 0; w < weeks; w++) {
            simulateWeek();
            deadLine.add(w+1,this.dead.size());
            recoveredLine.add(w+1,this.recovered.size());
            infectedLine.add(w+1,this.currentlyInfected.size());
            System.out.println(this.dead.size() + "\t" + currentlyInfected.size() + "\t" + recovered.size());
        }
        //save plot as png
        XYSeriesCollection lines = new XYSeriesCollection();
        lines.addSeries(deadLine);
        lines.addSeries(recoveredLine);
        lines.addSeries(infectedLine);
        JFreeChart chart = ChartFactory.createXYLineChart(d.name,"weeks","",lines, PlotOrientation.VERTICAL, true,false,false);
        try {
            ChartUtilities.saveChartAsPNG(image,chart,480,480);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        Disease corona = new Disease("Covid19",3,0.02,2);
        Pandemic p = new Pandemic(100_000,corona);
        //Exception-handeling for ArrayIndexOutOfBounds
        try {
            File image = new File(args[1] + File.separator + corona.name + ".png");
        p.simulate(20, image);}
        catch(ArrayIndexOutOfBoundsException e){
            e.getMessage();
        }
    }
}

