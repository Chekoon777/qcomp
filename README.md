# QuantumCraft: The Blockverse Simulator

![QuantumCraft](https://github.com/Chekoon777/qcomp/assets/113446650/e49776c0-0e93-4898-a676-f5de9eb44d0e)
------------------------

### Group QD
### Byungchul Kim, Chaehoon Park, Nohyoon Park, Taewoo Lee
------------------------

## Source Code Directory


src/main/java/qd.qcomp.qcompplugin


## Source Code Files

Qcomp.java
MetaManager.java
Qstate.java
Qubit.java
Gate.java


## Goals

Understanding quantum computing and circuits in an interactive, visual Format
Make it more accessible and comprehensible to a broader audience
Provide an engaging learning experience
Inspire future generations of scientists and programmers to explore the fascinating world of quantum computing


## Implemented Features

1. Real-Time Circuit Modification

2. Displays Quantum State on Every Circuit Blocks<br /><br />
coef of zero ket and one ket shows probability of measuring 0 and 1

3. Ifzero and Ifone Viewable When Block Clicked on Every Circuit Blocks<br /><br />
Ifzero and Ifone are lists that store quantum states of other qubits that are controlled by this qubit, when this qubit is zero and one respectively.<br />
When Measurement, either Ifzero or Ifone is propagated into other qubits, in the reverse order of qubits becoming related.

4. Commands:<br /><br />
/qskit: Gives player blocks for constructing a quantum circuit<br />
/qsinit: Changes the initial quantum state into a custom state<br />
/customgate: Edits the entries of custom quantum gate A


## Example of What You Can Make

### Basic Quantum Entanglement Circuit

<img width="1280" alt="3" src="https://github.com/Chekoon777/qcomp/assets/113446650/93c30cab-bd03-45a3-a4bf-b48b2fd7184b">

Above image is the in-game circuit implementation. At both 0th qubit and 1st qubit, 0 ket and 1 ket has coefficient of 0.717, which is 1/sqrt(2), showing 50:50 probability of measuring 0 and 1.

<img width="656" alt="4" src="https://github.com/Chekoon777/qcomp/assets/113446650/3e8a481c-7b31-4057-a32a-135de6c96c44">

When a certain qubit is clicked, a window appear showing other affected qubits. 'Ifzero' and 'Ifone' shows the state of other qubit after measuring this qubit as zero or one, respectively. This calculation is based on Born's Rule.

### Quantum Oracle in Deutsch's Problem

- Quantum Oracle with constant function

<img width="1280" alt="1" src="https://github.com/Chekoon777/qcomp/assets/113446650/50c91cb6-933f-4364-a0e9-69e2a1ab2475">

In this example, the oracle function is constant (f(0)=1, f(1)=1). Therefore, the input state at the end has still 100% probability of measuring zero. <br />

- Quantum Oracle with balanced function

<img width="1280" alt="2" src="https://github.com/Chekoon777/qcomp/assets/113446650/901098e8-fa59-4a81-953a-6c8963a21c7a">

In this example, the oracle function is balanced (f(1)=0, f(0)=1). Therefore, the input state at the end has still 100% probability of measuring one.<br />

- Deutsch-Josza Algorithm

When quantum oracle is expanded into one that get 3 or more input qubits, quantum oracle of Deutsch-Josza is implemented. You will be able to find the input state after applying quantum oracle remaining all zero when if the oracle function is constant, and changing into non-zero when if the oracle function is balanced.
