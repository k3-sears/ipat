/*
 * 
 */
package Algorithms;

import Src.Controller;
import Src.SolutionAttributes;
import Src.Kernel;
import Src.Profile;
import Src.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * The Class ESEvolution.
 */
public class ESEvolution implements MetaHeuristic {

    private ArrayList<Profile> best = new ArrayList<>(); //holds copies of all the current best solutions
    private ArrayList<Profile> nextGen = new ArrayList<>(); //holds copies of all the new generation of  solutions

    /**
     * F1.
     *
     * @param x the x
     * @return the double
     */
    private double F1(double x) {

        double F1val = x * 10;
        //0.106 - 0.0713 * x * x * x + 1.6707 * x * x - 14.6554 * x + 50.6783;
        if (F1val > 100) {
            F1val = 100;
        }
        if (F1val < 0) {
            F1val = 0;
        }
        F1val = 0.5 - F1val / 200.0;//truncate to range 0-0.5

        return (F1val);
        //return (2 * F1val / 100);
        // "+ 0.106" : so F1(10) in not negative
    }

    /**
     * mutateProfile.
     *
     * @param prof the prof
     * @param mutation_rate the mutation_rate
     * @return true, if successful
     */
    private boolean mutateProfile(int which, double mutation_rate) {

        int numberofactivekernels = 0;
        double newval;

        String profilename = nextGen.get(which).getName();
        //   System.out.println("in evolution.mutateprofile() name of profile nextgen[" +which +"] is " + profilename);
        //  System.out.println(".......mutation parameter is " +mutation_rate);
        HashMap kernels = nextGen.get(which).getKernels();
        // System.out.println(".....the number of kernels is " + kernels.size());
        Collection values = kernels.values();
        Iterator iterateKernels = values.iterator();
        SolutionAttributes currentVariable = null;
        String currentvarname;
        // loop through each kernel in turn,
        while (iterateKernels.hasNext()) {
            //get the next kernel
            Kernel kernel = (Kernel) iterateKernels.next();

            //get all of its variables
            HashMap vars = kernel.getVariables();

            Set keySet1 = vars.keySet();
            Iterator eVar = keySet1.iterator();
          //   System.out.println(".....Kernel " + kernel.getName() + " has " + vars.size() + " elements");

            // and then mutate each of the variables within kernel in turn
            while (eVar.hasNext()) {
                currentvarname = eVar.next().toString();
                currentVariable = (SolutionAttributes) vars.get(currentvarname);
                newval = mutateVariable(currentVariable, mutation_rate);
                if (newval != currentVariable.getValue()) {
                 //     System.out.println("mutating variable " + currentvarname + " in kernel " + kernel.getName());
                    //     System.out.println("... old value " + currentVariable.getValue() + " is changing  to " + newval);

                    // change value in local copy of variable
                    currentVariable.setValue(newval);
                  //   System.out.println("........have set value in currentVariable ");

                    //change value in local copy of hashmap
                    vars.put(currentvarname, currentVariable);

                    //currentVariable = (SolutionAttributes) vars.get(currentvarname);
                    //System.out.println("Value in vars is now" + currentVariable.getValue()    );
                }
            }
            // finally mutate the probability that the kernel is active
            if (Utils.GetRandDouble01() < mutation_rate) {
                // mutatedProf.KernelFamily[k].active =
                // (mutatedProf.KernelFamily[k].active = true)? false:true;
            }
            // Aug 2011

            //finally need to write this new kernel back to the profile in the  nextGen arraylist
            // ###############################################################################################################################
            //delete the old one the add the new one
            //nextGen.get(which).removeKernel(kernel.getName());
            nextGen.get(which).addKernel(kernel);

        }// end of loop mutating individual kernels
        // need to ensure that enough kernels are still active
		/*
         * while(numberofactivekernels< Attributess.minKernels) { chosen =
         * IpatUtils.GetRandIntInRange(1,MAX_KERNELS);
         * if(mutatedProf.KernelFamily[chosen].active ==false) {
         * mutatedProf.KernelFamily[chosen].active = true;
         * numberofactivekernels++; } }
         */

        HashMap vars = nextGen.get(which).getSolutionAttributes();
        // System.out.println(".....the number of profile variables is " + vars.size());
        Set keySet2 = vars.keySet();
        Iterator pVar = keySet2.iterator();
        // finally the profile level variables
        // for(int pvar=0;pvar<vars.size();pvar++)
        while (pVar.hasNext()) {
            currentvarname = pVar.next().toString();
            currentVariable = (SolutionAttributes) vars.get(currentvarname);
            newval = mutateVariable(currentVariable, mutation_rate);
            if (newval != currentVariable.getValue()) {
                //   System.out.println("mutating profile variable " + currentvarname );
                //   System.out.println("... old value " + currentVariable.getValue() + " is changing  to " + newval);
                //set the new value in the local copy of the variable
                currentVariable.setValue(newval);
                //   System.out.println("........have set value in currentVariable ");
                //replace it in the local hash table
                vars.put(currentvarname, currentVariable);
                currentVariable = (SolutionAttributes) vars.get(currentvarname);
                //  System.out.println("..............Value in vars is now" + currentVariable.getValue()    );
                //  System.out.println("....now changing the profile in the nextgen arraylist");
                //and replace (remove-add) the old variable in the profile in the nextGenarray with the one one

                nextGen.get(which).addVariable(currentVariable);
                //HashMap tmpvars = nextGen.get(which).getSolutionAttributes();
                //currentVariable = (SolutionAttributes) tmpvars.get(currentvarname);
                // System.out.println("..............Value in nextGen is now" + currentVariable.getValue()  );

            }
        }

        //finally write the mutated profile back to file
        //nextGen.get(which).writeProfileToFile(profilename);
        //nextGen.get(which).printProfile();
        //System.out.println("finished mutating profle" + which);
        return true;
    }

    /**
     * mutateVariable.
     *
     * @param variableToChange the variable to be changed
     * @param mutation_rate the mutation_rate
     * @return new value for variable
     */
    private double mutateVariable(SolutionAttributes variableToChange, double mutation_rate) {
        double oldVal = variableToChange.getValue();
        int possibilities, chosen;
        double stepsize, dchosen, myrand;
        double newValue = 0;

        //pick a random numnber
        myrand = Utils.GetRandDouble01();
        //System.out.println("my random number is " + myrand);

        //the way that mutation paramter is interpreted, and mutation worksd, depedns on the type of variable
        if (variableToChange.getType().equalsIgnoreCase("boolean")) {
            if (myrand < mutation_rate * variableToChange.getRateOfEvolution()) {//Boolean - mutation sets 1s to 0s and vice versa
                if (variableToChange.getValue() == 1.0) {
                    newValue = 0.0;
                } else {
                    newValue = 1.0;
                }
                //System.out.println("..................flipping binary variable " + variableToChange.getName() + "to value " + variableToChange.getValue()) ;
            }
        } else if (variableToChange.getType().equalsIgnoreCase("cardinal")) {
            if (myrand < mutation_rate * variableToChange.getRateOfEvolution()) {// a list of different catgorical values with no natural oerdering so just pick a new value at random

                // how many discrete values could this variable take?
                possibilities = (int) ((variableToChange.getUbound() - variableToChange.getLbound()) / variableToChange.getGranularity());
                // if the values were indexed chose one index at random
                chosen = Utils.GetRandIntInRange(0, possibilities);
                // now compute what actual value this would be
                newValue = variableToChange.getLbound() + variableToChange.getGranularity() * chosen;
                //System.out.println("...............randomly choosing new value " + newValue + "for cardinal variable " + variableToChange.getName() );
            }
        } else if (variableToChange.getType().equalsIgnoreCase("ordinal")) {
            // ordinal varibles - for exanmple continuos variables of integers where sequence counts 
            //mutation rate is interpreted as a normalised step size
            // factor first pick N(0,stepsize) deviate, then convert to the
            // allowed granularity
            stepsize = (variableToChange.getUbound() - variableToChange.getLbound()) * mutation_rate * variableToChange.getRateOfEvolution();
            dchosen = Utils.GetGaussN01Double() * stepsize;
            dchosen = variableToChange.getGranularity() * Math.floor(0.5 + dchosen / variableToChange.getGranularity());
            newValue = oldVal + dchosen;
            if (newValue < variableToChange.getLbound()) {
                newValue = variableToChange.getLbound();
            }
            if (newValue > variableToChange.getUbound()) {
                newValue = variableToChange.getUbound();
            }
            //System.out.println("...................choosing new value " + newValue + "for ordinal variable " + variableToChange.getName() );
        } else {
            System.out.println("Error - unkown variable type " + variableToChange.getType() + "for variable " + variableToChange.getName());
        }
        return newValue;
    }

    /**
     *
     * @param which
     * @return
     */
    @Override
    public Profile getNextGenProfileAtIndex(int which) {
        if (which < 0) {
            throw new UnsupportedOperationException("tried to acces nextGen item with negative index");
        } else if (which > nextGen.size()) {
            throw new UnsupportedOperationException("tried to acces nextGen item with index " + which + "but there are only" + best.size());
        } else {
            //System.out.println("in evolution.getNextGenProfileAtIndex with index: " + which );
            //File thisfile = nextGen.get(which).getFile();
            //System.out.println("... nextgen profile name is: " + nextGen.get(which).getName() + " and filename " + thisfile.getName());
            //return getProfileFromFile(thisfile);

            Profile toreturn = nextGen.get(which);
            return toreturn;
        }

    }

    /**
     *
     * @param howMany
     */
    @Override
    public void generateNextSolutions(int howMany) {

        int copied, toCopy;
        //check that the working memory is not empty
        if (best.size() <= 0) {
            throw new UnsupportedOperationException("Can't call generateNextSolutions without calling UpdateWorkingMemory() First");
        }
        //clear  the array nextGen
        nextGen.clear();

        //if the user has resized the population so that we have more "best" solutions than we want new profiles
        // then we must lose some of our "best" solutions at random
        while (best.size() > howMany) {
            best.remove(Utils.GetRandIntInRange(0, howMany));
        }

        //make at least one copy of all the best, and howMany in total
        for (copied = 0; copied < howMany; copied++) {
            if (copied < best.size())//at least one copy of each
            {
                toCopy = copied;
            } else if (best.size() == 1)//if theres only one clone it repeatedly
            {
                toCopy = 0;
            } else //otherwise fill up with clones of randomly selected members of best
            {
                toCopy = Utils.GetRandIntInRange(0, best.size() - 1);
            }
            //copy all the profiles from the  set of the previous best 
            // create new profile object so that adding best to nextGen doesnt just reference the same object
            File thisfile = best.get(toCopy).getFile();
            Profile toAdd = getProfileFromFile(thisfile);
            nextGen.add(toAdd);
            System.out.println("have made a copy of best[" + copied + "] with filename " + thisfile.getName());
        }

        // apply mutation where necessary - i.e. leaving one dulicate of each of the best
        for (int toMutate = best.size(); toMutate < howMany; toMutate++) {
            //decide on a mutation rate parameter  according to how the user rated it.  We can use fixed rates to test the operation of the EA
            //double rateToApply = 0.5; 
            // double rateToApply = 1.0; 
            double rateToApply = this.F1(nextGen.get(toMutate).getGlobalScore());
            //System.out.println("global score for the profile " + nextGen.get(toMutate).getName() 
            //        + " is " + nextGen.get(toMutate).getGlobalScore() 
            //       + " and mutation parameter is " + rateToApply);
            //now apply mutation with this parameter
            this.mutateProfile(toMutate, rateToApply);

            //System.out.println("..... mutate profile " + toMutate + " complete");
        }

        //make the folder to hold the files in which we will store the next generation
        File file = new File(Controller.outputFolder.getAbsolutePath() + "/generations/");
        file.mkdir();

        for (Profile NG : nextGen) {
            System.out.println("I am An NG " + NG.getName());
        }
        int generation = 0;
        for (int k = 0; k < nextGen.size(); k++) {
            
            try
            {
            Profile profile = nextGen.get(k);
            String profileName = profile.getName();
            System.out.println("at start of loop for k: " + k + ": ProfileName = " + profileName);
            //get base name for profile
            String profileTemplate = profileName.substring(profileName.indexOf('-'), profileName.lastIndexOf('_') + 1); // should be of form "profile_"

            System.out.println("bwfore reading generation  has value " + generation);
            //get generation and increment it
            generation = Integer.parseInt(profileName.substring((profileName.indexOf('_') + 1), profileName.indexOf('-')));
                       System.out.println("after reading generation  has value " + generation);
            generation++;
            String outProfileName = "gen_" + generation + profileTemplate + k + ".xml";
            System.out.println("outprofilename = " + outProfileName + "\n");

            // set name in profile to match new name
            nextGen.get(k).setName(outProfileName);

            // write out the profile to file for safe keeping
            //build the path by fetching the session details from the controller and adding generaios + this file name
            String outProfilePath = Controller.outputFolder.getAbsolutePath() + "/generations/" + outProfileName;

            //write to file
            nextGen.get(k).writeProfileToFile(outProfilePath);
            File thisfile = new File(outProfilePath);
            nextGen.get(k).setFile(thisfile);
            
              profileName = profile.getName();
            System.out.println("at end of loop for k: " + k + ": ProfileName = " + profileName +"\n\n");
            
            
            }
            catch (StringIndexOutOfBoundsException ex) {
                System.out.println("The profile names do not follow the correct convention to be processed."
                      + "/nLook within the Profiles Folder, and ensure the names appear as: gen_0-Profile_x.xml");
                System.out.println(ex.getMessage());
        }
    }
    }
//        // finally update profile names by incrementing the generation count in each name and write them to file for safe keeping
//         for (int i = 0; i < nextGen.size(); i++) 
//          {
//              
//            try {
//                String profileName = nextGen.get(i).getName(); // should be of form "gen_x-profile_y.xml"
//                System.out.println("ProfileName = " + profileName);
//                //get base name for profile
//                String profile = profileName.substring(profileName.indexOf('-') , profileName.lastIndexOf('_')+1); // should be of form "profile_"
//                System.out.println("Profile = " + profile);
//                //get generation and increment it
//                int generation = Integer.parseInt(profileName.substring((profileName.indexOf('_') + 1), profileName.indexOf('-')));
//                generation++;
//                
//                //build string holding new name - note that we hav not kept traack of the profile indices
//                String outProfileName = "gen_" + generation + profile + i + ".xml";
//                System.out.println("outprofilename = " + outProfileName);
//
//                // set name in profile to match new name
//                nextGen.get(i).setName(outProfileName);
//            
//                // write out the profile to file for safe keeping
//                //build the path by fetching the session details from the controller and adding generaios + this file name
//                String outProfilePath = Controller.outputFolder.getAbsolutePath() + "/generations/" + outProfileName;
//                
//                //write to file
//                nextGen.get(i).writeProfileToFile(outProfilePath);
//                File thisfile = new File(outProfilePath);
//                nextGen.get(i).setFile(thisfile);
//   
//            } catch (StringIndexOutOfBoundsException ex) {
//                System.out.println("The profile names do not follow the correct convention to be processed."
//                        + "/nLook within the Profiles Folder, and ensure the names appear as: gen_0-Profile_x.xml");
//                System.out.println(ex.getMessage());
//            }
//        }
//System.out.println("changed names and saved files");
//finally write all ofthe profiles to file for safe keeping
//for(int toSave=0; toSave < howMany;toSave++)
//{
//  String outProfileName= nextGen.get(toSave).getName();
// String outProfilePath = Controller.outputFolder.getAbsolutePath() + "/generations/" + outProfileName;
//System.out.println("saving next gen profile to file: " + outProfileName);
// nextGen.get(toSave).writeProfileToFile(outProfilePath);
//}
//}
// TODO read in the global scores from the profile.xml files
    /**
     *
     * @param file
     * @return
     */
    public Profile getProfileFromFile(File file) {
        Profile profile = new Profile(file);
        try {
            Document XmlDoc = new SAXBuilder().build(file);

            Element root = XmlDoc.getRootElement();
            Element profileNode = root.getChild("profile", root.getNamespace());
            Iterator iterator = profileNode.getChildren().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Element hint = (Element) iterator.next();
                if (hint.getName().equalsIgnoreCase("variable")) {
                    String name = hint.getChildText("name");
                    String type = hint.getChildText("type");

                    String temp = hint.getChildText("lbound");
                    Double dub = new Double(temp);
                    double lbound = dub.doubleValue();

                    temp = hint.getChildText("ubound");
                    dub = new Double(temp);
                    double ubound = dub.doubleValue();

                    temp = hint.getChildText("granularity");
                    dub = new Double(temp);
                    double granularity = dub.doubleValue();

                    temp = hint.getChildText("rateOfEvolution");
                    dub = new Double(temp);
                    double rateOfEvolution = dub.doubleValue();

                    temp = hint.getChildText("value");
                    dub = new Double(temp);
                    double value = dub.doubleValue();

                    String dfault = hint.getChildText("default");
                    String flag = hint.getChildText("flag");
                    String unit = hint.getChildText("unit");

                    SolutionAttributes variable = new SolutionAttributes(name, type,
                            lbound, ubound, granularity, rateOfEvolution, value, dfault, flag, unit);
                    profile.addVariable(variable);
                } else if (hint.getName().equalsIgnoreCase("kernel")) {

                    Iterator it = hint.getChildren().iterator();
                    Element nm = (Element) it.next();
                    String kernelName = nm.getText();
                    HashMap vars = new HashMap();
                    while (it.hasNext()) {
                        Element hintt = (Element) it.next();
                        String name = hintt.getChildText("name");
                        String type = hintt.getChildText("type");

                        String temp = hintt.getChildText("lbound");
                        Double dub = new Double(temp);
                        double lbound = dub.doubleValue();

                        temp = hintt.getChildText("ubound");
                        dub = new Double(temp);
                        double ubound = dub.doubleValue();

                        temp = hintt.getChildText("granularity");
                        dub = new Double(temp);
                        double granularity = dub.doubleValue();

                        temp = hintt.getChildText("rateOfEvolution");
                        dub = new Double(temp);
                        double rateOfEvolution = dub.doubleValue();

                        temp = hintt.getChildText("value");
                        dub = new Double(temp);
                        double value = dub.doubleValue();

                        String dfault = hintt.getChildText("default");
                        String flag = hintt.getChildText("flag");
                        String unit = hintt.getChildText("unit");

                        SolutionAttributes variable = new SolutionAttributes(name, type,
                                lbound, ubound, granularity, rateOfEvolution, value, dfault,
                                flag, unit);
                        vars.put(name, variable);
                    }
                    Kernel kernel = new Kernel(kernelName, vars);
                    profile.addKernel(kernel);
                } else if (hint.getName().equalsIgnoreCase("interaction")) {
                    // TODO 
                }
            }
        } catch (Exception pce) {
            pce.printStackTrace();
        }
        return profile;
    }

    /**
     *
     * @param evaluatedSolutions
     */
    @Override
    public void updateWorkingMemory(Profile[] evaluatedSolutions) {
        int popmember = 0; //loop variable
        //this is an EA so we are going to start by clearing the previous population if it isnt the first
        best.clear();
        //now we want to find out what the best fitness seen is
        double bestFitness = 0.0;
        for (popmember = 0; popmember < evaluatedSolutions.length; popmember++) {
            if (evaluatedSolutions[popmember].getGlobalScore() > bestFitness) {
                bestFitness = evaluatedSolutions[popmember].getGlobalScore();
            }
        }
        //finally see which of our evaluated solutions are the equal best and add them to the list.
        for (popmember = 0; popmember < evaluatedSolutions.length; popmember++) {
            if (evaluatedSolutions[popmember].getGlobalScore() >= bestFitness) {
                best.add(evaluatedSolutions[popmember]);
            }
        }
        // TESTING : check to see if the fitness values are being evaluated and assigned to "Best" List
//        for (Profile best1 : best) {
//            System.out.println("those assigned as best in ESEvolution.updateWorkingMemory(): " + best1.getName() + " : " + best1.getGlobalScore());
//        }
    }

}
