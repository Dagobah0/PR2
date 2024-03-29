package main.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Graph {

    private Node[] graph;
    private List<Node> nodeWithPods;
    private Node HQ;
    private Node enemyHQ;
    private List<Node> strategicNodes;

    private int[][] lengthBetweenZones;
    private Timer timer;

    public Graph(int size) {
        graph = new Node[size];
        nodeWithPods = new ArrayList<>();
        strategicNodes = new ArrayList<>();
        HQ = null;
        enemyHQ = null;
    }


    //Getters

    public Node getNode(int id){ return graph[id]; }

    public List<Node> getNodesWithPods() { return nodeWithPods; }

    public Node getHQ() { return HQ; }

    public Node getEnemyHQ() { return enemyHQ; }

    //Setters

    public void setQG(Node node) { HQ = node; }

    public void setEnemyHQ(Node node) { enemyHQ = node; }

    /**
     * Add node in the graph
     * @param node to add in the graph
     */
    public void addNode(Node node) { graph[node.getId()] = node; }

    /**
     * Create a link between nodeA and nodeB
     * @param nodeA
     * @param nodeB
     */
    private void addLinkBetweenNodes(Node nodeA, Node nodeB) {
        nodeA.addLinkedNode(nodeB);
        nodeB.addLinkedNode(nodeA);
    }

    /**
     * Create a link between nodeA and nodeB from their id
     * @param nodeIdA
     * @param nodeIdB
     */
    public void addLinkBetweenNodesFromId(int nodeIdA, int nodeIdB){
        addLinkBetweenNodes(this.getNode(nodeIdA), this.getNode(nodeIdB));
    }

    /**
     * Add node to the list of node which contains pods
     * @param node
     */
    public void addNodeWithPods(Node node){
        if (!nodeWithPods.contains(node)){
            nodeWithPods.add(node);
        }
    }

    /**
     * Update the pods number for the given node and update the list of node with pods in consequence
     * @param playerID
     * @param node
     * @param pods
     * @param enemyPods
     */
    public void updatePods(int playerID, Node node, int pods, int enemyPods){
        if (playerID == 0) {
            node.setPodsNumber(pods);
            node.setEnemyPodsNumber(enemyPods);
        } else {
            node.setPodsNumber(enemyPods);
            node.setEnemyPodsNumber(pods);
        }
        if(node.getPodsNumber() > 0){
            addNodeWithPods(node);
        }
        else {
            removeNodeWithPods(node);
            PodsManager.getInstance().removePod(PodsManager.getInstance().getPodOnNode(node));
        }
    }

    /**
     * Remove the given node from the list of node with pods
     * @param node
     */
    public void removeNodeWithPods(Node node){ nodeWithPods.remove(node); }

    public Node getNeighbourWithMaxPlatinium(Node node){
        Node ret = null;
        int quantity = 0;
        for(Node n : node.getLinkedNodes()) {
            if(n.getPlatinumProduction() > quantity) {
                ret = n;
                quantity = n.getPlatinumProduction();
            }
        }
        return ret;
    }

    public void updateNodesInterest(Pod p) {
        for (Node n : p.getNodeOn().getLinkedNodes()) {
            float interest = 0;
            if (n.getPlatinumProduction() != 0 && n.getOwnerID()!= StrategyManager.getInstance().getPlayerID()) interest += 1 + n.getPlatinumProduction();
            for (Node node : n.getLinkedNodes()){
                interest += node.isVisible();
            }
            if(n.isNeutral()) interest *=10;
            if(n.getOwnerID() == (StrategyManager.getInstance().getPlayerID()^1)) interest *=20;
            if(n.isTargeted() || p.getLastTarget() == n) interest = 0;
            if(n.getInterest() < 0 ) n.setInterest(interest);
        }
    }

    public void resetNodeInterest() {
        for(Node n : graph) {
            n.setInterest(-1);
        }
    }

    /**
     * TEST METHOD : WILL BE DELETE
     * Give a random node from the graph
     * @return random Node from the graph
     */
    public Node getRandomNode() {
        Random r = new Random();
        return graph[r.nextInt(graph.length)];
    }

    public void calculateLengthBetweenZones(){
        timer = new Timer();
        for (Node n1 : graph) {
            for(Node n2 : graph) {
                if (!n2.equals(n1) && lengthBetweenZones[n1.getId()][n2.getId()] == 0) {
                    lengthBetweenZones[n1.getId()][n2.getId()] = PathFinding.BFS(n1,n2).size();
                    lengthBetweenZones[n2.getId()][n1.getId()] = lengthBetweenZones[n1.getId()][n2.getId()];
                }
            }
            timer.displayDeltaTime();
        }
    }

    public void setStrategicNodes() {
        for (Node n : graph) {
            if (PathFinding.BFS(HQ, n).size() == PathFinding.BFS(enemyHQ, n).size()) {
                strategicNodes.add(n);
            }
        }
    }

    public List<Node> getStrategicNodes() { return strategicNodes; }



    //DEBUG
    public Node[] getGraph() { return graph; }

    public void displayStrategicNodes() {
        for (Node n : strategicNodes) {
            System.err.println(n.getId());
        }
    }

    public void displayInterest() {
        for (Node n : graph) {
            System.err.println(n.getId() + " " + n.getInterest());
        }
    }
}
