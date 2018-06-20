package com.epeg.Study;

/**
 * Created by pschrempf on 15/03/16.
 *
 * Class that stores a singly linked list of pegs, keeping track of the head and tail of the list.
 */
public class PegList {

    private Peg head;
    private Peg tail;

    /**
     * Constructs an empty peg list.
     */
    public PegList(){
        this(null);
    }

    /**
     * Constructs a list given a head Peg.
     *
     * @param head - peg to place at head of list
     */
    public PegList(Peg head){
        this.head = head;

        // find tail
        while (head != null && head.next != null)
            head = head.next;

        this.tail = head;
    }

    /**
     * Adds a peg to the tail list.
     *
     * @param peg - peg to be added
     */
    public void addLastPeg(Peg peg) {
        if (head == null) {
            head = peg;
            tail = peg;
        } else {
            tail.next = peg;
            tail = peg;
        }
    }

    /**
     * Adds a peg to the head of the list.
     *
     * @param peg - peg to be added
     */
    public void addFirstPeg(Peg peg) {
        if (head == null) {
            head = peg;
            tail = peg;
        } else {
            peg.next = head;
            head = peg;
        }
    }

    /**
     * Gets the head of the list.
     *
     * @return Peg at head of list.
     */
    public Peg getHead() {
        return head;
    }

    /**
     * Gets the tail peg of the list.
     *
     * @return Peg at the tail of the list.
     */
    public Peg getTail() {
        return tail;
    }
}
