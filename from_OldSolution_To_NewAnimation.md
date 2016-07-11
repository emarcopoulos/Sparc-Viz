# The original drawing java file flow

### _KEY_: '[function]' indicate the function which will be detailed in the next indentation of the list.

- call [main] with sparc program path
  - create storage for vizAtoms
  - get result for sparc program
  - [parseResult] into AnswerSet
    - parse with DLVAnswerSetParser
    - method [getAnswerSets]
      - add all characters inbetween '{' and '}' to a string ('{}' indicates an answerSet. repeat the following steps for every occurence of '{}')
      - create an answerSet to store string.
      - [splitCommaSequence] on string to get array of strings of predicates and add it to the created answerSet's literals
        - create array of predicates (called arguments), paranthesis counter, and a lastBeginIndex (keeps track of index of current predicate)
        - run through the given string, checking for parenthesis as indicators of predicate start and end, if the predicate has ended, create a substring from the current index and the lastBeginIndex to get the predicate and add it to array of predicates
        - store last predicate string
        - return list of predicates
      - return array of all answerSets found
    - return array of answerSets
  - call [getVizAtoms] on literals of answerSet
    - for each literal determine if predicate is a visual keyword, and if so add to array of vizatoms
    - uses [getPredicate]
      - runs through each literal until first '(' which indicates end of predicate name, and returns that substring
    - return vizatoms
  - check for correct usage of vizatoms with [isTrueVisual]
    - for each vizAtom, uses getPredicate to access predicate, then checks if _condition_ (the following are conditions) is met, and prints an error if not. Conditions:
    - correct number of elements (uses [elementNum])
      - return number of commas+1 (this function does not actually work for a generic predicate)
    - number elements are within canvas and other required bounds (uses [getElement])
      - use open parenthesis to indicate start of first parameter, and comma for successive ones (this function does not actually work for a generic predicate)
      - return element found
    - return true if all conditions met
  - print corresponding html drawing commands

## Plan to change for new animation
- create function called getDrawingCommand which gets the drawing command of each animation command
- run through predicates of these drawing commands to ensure they are actual drawing commands
- use the same exact condition tests to see if isTrueVisual elements
- change html printing statements into statements that add strings to an array
- build array where each index of array corresponds to a frame and consists of an array of commands to be executed specified by the drawing commands specified by the sparc program.
- print corresponding html program