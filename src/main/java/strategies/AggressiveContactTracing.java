package strategies;

import com.epidemic_simulator.Person;
import com.epidemic_simulator.Simulator;
import com.epidemic_simulator.Strategy;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;


public class AggressiveContactTracing extends Strategy {
    private final HashSet<Person> immunes = new HashSet<>();
    private final HashSet<Person> positives = new HashSet<>();
    private final HashMap<Person, Integer> quarantineMap = new HashMap<>();

    private final HashSet<Person> toRemove = new HashSet<>();
    private final HashMap<Person, Integer> toQuarantine = new HashMap<>();

    public AggressiveContactTracing(Simulator simulator) {
        super(simulator);
    }

    @Override
    public void afterExecuteDay(Simulator.Outcome outcome) {
        //for each quarantined person
        for (Person key : quarantineMap.keySet()) {
            //lowering their quarantine days count
            int newQuarantineDays = quarantineMap.get(key) - 1;

            if (newQuarantineDays == 0) {
                //if he was already tested then it means he's now cured since it's second quarantine has ended
                if (positives.contains(key)) {
                    positives.remove(key);
                    personClean(key);
                    //if he was not already tested then i test him, and release him if he's clear
                } else if (!simulator.testVirus(key)) {
                    System.out.println(key + " RELEASE!");
                    toRemove.add(key);
                    key.canMove = true;
                    //if he's not clean it means he's infected, so i put him on quarantine till the day that he will heal.
                } else {
                    System.out.println("DAY (" + simulator.getDay() + "): " + key + " IS INFECTED, DO NOT RELEASE!");
                    System.out.println("KEEP HIM LOCKED FOR " + (simulator.diseaseDuration - simulator.canInfectDay) + " days");
                    quarantineMap.put(key, simulator.diseaseDuration - simulator.canInfectDay);
                    positives.add(key);
                    quarantineContacts(key, simulator.diseaseDuration - simulator.canInfectDay, toQuarantine);
                }
            } else {
                //if they didn't reach 0 i just save the new day.
                quarantineMap.put(key, newQuarantineDays);
            }
        }
        //remove the ones i had to remove inside the for
        toRemove.forEach(quarantineMap::remove);
        //add the new quarantined
        toQuarantine.forEach(this::quarantine);

        toRemove.clear();
        toQuarantine.clear();
    }

    @Override
    public void personClean(Person person) {
        System.out.println("DAY (" + simulator.getDay() + "): " + person + " IS NOW CLEAN!");
        immunes.add(person);
        person.canMove = true;
    }

    @Override
    public void personHasSymptoms(Person person) {
        super.personHasSymptoms(person);

        quarantineContacts(person, simulator.canInfectDay, quarantineMap);
    }

    public void quarantineContacts(Person person, int daysBackward, HashMap<Person, Integer> outputMap) {
        for (Person contact : findEncounters(person, daysBackward)) {
            if (!immunes.contains(contact) && !positives.contains(contact)) {
                System.out.println("QUARANTINED: " + contact + "(" + simulator.getDay() + ") FOR (" + simulator.canInfectDay + " days)");
                outputMap.put(contact, simulator.canInfectDay);
                contact.canMove = false;
            }
        }
    }

    public void quarantine(Person person, int days) {
        if (!positives.contains(person) && !immunes.contains(person)) {
            quarantineMap.put(person, simulator.canInfectDay);
            person.canMove = false;
            System.out.println("QUARANTINED: " + person + "(" + simulator.getDay() + ") FOR (" + days + " days)");
        }
    }

}
