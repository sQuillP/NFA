import java.util.*;
import java.io.*;


/*
* William Pattison
* IT328
*/


/*
* NFA2DFA will convert a given NFA text file into
* a DFA that accepts the same langauge. Techniques that
* are used to solve this include nested ArrayLists, sets,
* and recursion to find delta transitions for each state.
*/


public class NFA2DFA
{


  /*Nested arraylist containing all the states and their transition values*/
  private ArrayList<ArrayList<Set<Integer>>> NFA = new ArrayList<ArrayList<Set<Integer>>>();

  /*Nested ArrayList to contain all the newly converted NFA states*/
  private ArrayList<ArrayList<Integer>> DFA = new ArrayList<ArrayList<Integer>>();

  /*Final states */
  private ArrayList<Integer> NFA_finalStates = new ArrayList<Integer>();

  /*Final states for DFA*/
  private Set<Integer> DFA_finalStates = new HashSet<Integer>();

  /*Alphabet containing all input types for the NFA and DFA*/
  private ArrayList<String> sigma = new ArrayList<String>();

  /*New states containing the values*/
  private ArrayList<Set<Integer>> newStates = new ArrayList<Set<Integer>>();

  /*Hold the list of input strings*/
  private ArrayList<String> strings = new ArrayList<String>();

  /*Starting state of the NFA*/
  private int NFA_start;



  /*Constructor takes in filename*/
  NFA2DFA(String file1, String file2) { readFiles(file1,file2);}


  /*Return the set of values in the delta transition of a state given an alphabet.*/
  public void transition(Set<Integer> set,int state, String alphabet)
  {
    if(!alphabet.equals("lambda"))
      for(int val : NFA.get(state).get(sigma.indexOf(alphabet)))
        transition(set,val,"lambda");
    else
    {
      for(int val : NFA.get(state).get(NFA.get(state).size()-1))
      {
        if(state == val)
          set.add(val);
        else if(!set.contains(val))
          transition(set,val,"lambda");
      }
    }
  }


  /*Return true if set a is contained in b and if b is contained in a*/
  public boolean equalSets(Set<Integer> a, Set<Integer> b)
  {
    boolean equal = true;
    for(int element : a)
    {
      if(!b.contains(element))
      {
        equal = false;
        break;
      }
    }
    if(equal)
    {
      for(int element : b)
      {
        if(!a.contains(element))
        {
          equal = false;
          break;
        }
      }
    }
    return equal;
  }


  /*Check if the set obtained by the delta transition is a new state.*/
  private boolean isNewState(Set<Integer> s)
  {
    boolean isNewState = true;
    for(Set<Integer> element : newStates)
    {
      if(equalSets(element,s))
      {
        isNewState = false;
        break;
      }
    }
    return isNewState;
  }


  /*Return the state index from the new state table given a set obtained
  from a delta transition*/
  private int newStateIndex(Set<Integer> s)
  {
    int index = 0;
    for(Set<Integer> element : newStates)
    {
      if(equalSets(element,s))
        break;
      index++;
    }
    return index;
  }


  /*Algorithm for converting NFA to DFA*/
  public void convertNFA()
  {
    /*find lambda transition on the starting state, add it to the set of new states,
    and add 0 to DFA*/
    Set<Integer> s = new HashSet<Integer>();
    ArrayList<Integer> dfa_row;
    int newIndex = 0;
    transition(s,NFA_start,"lambda");
    newStates.add(s);
    /*Check if the first state is an accepting state*/
    for(int el : s)
    if(NFA_finalStates.contains(el))
      DFA_finalStates.add(0);
    /*Find delta transitions for every alphabet in every state*/
    for(int DFA_rows = 0; DFA_rows < newStates.size(); DFA_rows++)
    {
      dfa_row = new ArrayList<Integer>();
      for(String alphabet : sigma)
      {
        s = new HashSet<Integer>();
        /*Perform union*/
        for(int i : newStates.get(DFA_rows))
          transition(s,i,alphabet);
        /*Add the new state to the new state list if it did not exist before*/
        if(isNewState(s))
          newStates.add(s);
        /*Get the new value of the state*/
        newIndex = newStateIndex(s);
        /*Check if the new state contains an accepting state*/
        for(int state : s)
          if(NFA_finalStates.contains(state))
            DFA_finalStates.add(newIndex);
        /*add the newly added state from the alphabet input into DFA*/
        dfa_row.add(newIndex);
      }
      /*Add the new state transitions to the DFA*/
      DFA.add(dfa_row);
    }
   }


   /*Validate a string input using DFA*/
  private void validateInputs()
  {
    int currentState, index, counter = 0;
    boolean valid = true;
    for(String input : strings)
    {
      currentState = 0;
      for(int i = 0; i<input.length(); i++)
      {
        index = sigma.indexOf(Character.toString(input.charAt(i)));
        if(index != -1)
          currentState = DFA.get(currentState).get(index);
        else
        {
          currentState = -1;
          break;
        }
      }
    if(DFA_finalStates.contains(currentState))
      System.out.print("Yes ");
    else
      System.out.print("No  ");
    if(++counter % 15 == 0)
      System.out.println();
    }
  }


  /*Print the results of the NFA to DFA conversion*/
  public void results()
  {
    // System.out.println("\n"+nfa_file+" to DFA");
    convertNFA();
    printDFA();
    for(int i : DFA_finalStates)
      System.out.print(i+" ");
    System.out.println(": Accepting State(s)\n");
    System.out.println("Parsing results from strings.txt");
    validateInputs();
  }



  /*Print the contents of the DFA*/
  public void printDFA()
  {
    System.out.print("\nSigma:      ");
    for(String alphabet : sigma)
      System.out.print(alphabet+"      ");
    System.out.println();
    for(int i = 0; i<=sigma.size(); i++)
    System.out.print("-------");
    System.out.println();
    for(int i = 0; i<DFA.size(); i++)
    {
      System.out.print("    "+i+":      ");
      for(int j = 0; j<DFA.get(i).size(); j++)
        System.out.print(DFA.get(i).get(j)+"      ");
      System.out.println();
    }
    for(int i = 0; i<=sigma.size(); i++)
      System.out.print("-------");
    System.out.println("\n0: Initial state");
  }


  /*Read in NFA and store it into program*/
  public void readFiles(String nfa_file, String input_file)
  {
    String line;
    int states;
    StringTokenizer st1, st2;
    Set<Integer> set;
    ArrayList<Set<Integer>> state;
    try{
      BufferedReader br = new BufferedReader(new FileReader(nfa_file));
      states = Integer.parseInt(br.readLine());
      line = br.readLine();
      st1 = new StringTokenizer(line," ");
      while(st1.hasMoreTokens())
        sigma.add(st1.nextToken());
      for(int i = 0; i<states; i++)
      {
        line = br.readLine();
        st1 = new StringTokenizer(line,": ");
        state = new ArrayList<Set<Integer>>();
        st1.nextToken();
        while(st1.hasMoreTokens())
        {
          st2 = new StringTokenizer(st1.nextToken(),"{,}");
          set = new HashSet<Integer>();
          while(st2.hasMoreTokens())
            set.add(Integer.parseInt(st2.nextToken()));
          state.add(set);
        }
        NFA.add(state);
      }
      NFA_start = Integer.parseInt(br.readLine());
      st1 = new StringTokenizer(br.readLine(),"{,}");
      while(st1.hasMoreTokens())
        NFA_finalStates.add(Integer.parseInt(st1.nextToken()));
      readInputs(input_file);
  } catch(IOException e){
      System.out.println("Unable to open file <"+nfa_file+"> <"+input_file+">");
      System.exit(0);
    }
  }


public ArrayList<String> getSigma(){return this.sigma;}

public ArrayList<ArrayList<Integer>> getDFA(){return this.DFA;}

public Set<Integer> getFinalStates(){return this.DFA_finalStates;}

public ArrayList<String> getInputs(){return this.strings;}


/*Read the strings.txt file and store them into program.*/
private void readInputs(String input_file)
{
  try
  {
    BufferedReader br = new BufferedReader(new FileReader(input_file));
    StringTokenizer st;
    String line;
    line = br.readLine();
    while(line!=null)
    {
      strings.add(line);
      line = br.readLine();
    }
  }
  catch(IOException e)
  {
    System.out.println("Unable to open file <"+input_file+">");
    System.exit(1);
  }
}


  /*Main to run NFA -> DFA conversion*/
  public static void main(String[] args)
  {
    if(args.length!=2)
      System.out.println("Use: java NFA2DFA <DFA file> <strings file>");
    else
    {
      NFA2DFA t = new NFA2DFA(args[0],args[1]);
      t.results();
    }
  }
}
