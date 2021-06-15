package org.heigit.ors.matrix.core;

import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.AllCHEdgesIterator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.ShortestWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import org.heigit.ors.common.Pair;
import org.heigit.ors.matrix.MatrixLocations;
import org.heigit.ors.matrix.MatrixMetricsType;
import org.heigit.ors.matrix.MatrixRequest;
import org.heigit.ors.matrix.MatrixResult;
import org.heigit.ors.matrix.algorithms.core.CoreMatrixAlgorithm;
import org.heigit.ors.routing.graphhopper.extensions.core.CoreTestEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.core.PrepareCore;
import org.heigit.ors.util.DebugUtility;
import org.heigit.ors.util.ToyGraphCreationUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

public class CoreMatrixTest {
    private final CarFlagEncoder carEncoder = new CarFlagEncoder();
    private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
    private final Weighting weighting = new ShortestWeighting(carEncoder);
    private final TraversalMode tMode = TraversalMode.NODE_BASED;
    private Directory dir;

    GraphHopperStorage createGHStorage() {
        return new GraphBuilder(encodingManager).setCHProfiles(new ArrayList<>()).setCoreGraph(weighting).create();
    }

    @Before
    public void setUp() {
        dir = new GHDirectory("", DAType.RAM_INT);
    }

    private CHGraph contractGraph(GraphHopperStorage g, CoreTestEdgeFilter restrictedEdges) {
        CHGraph lg = g.getCHGraph(new CHProfile(weighting, tMode, TurnWeighting.INFINITE_U_TURN_COSTS, "core"));
        PrepareCore prepare = new PrepareCore(dir, g, lg, restrictedEdges);

        // set contraction parameters to prevent test results from changing when algorithm parameters are tweaked
        prepare.setPeriodicUpdates(20);
        prepare.setLazyUpdates(10);
        prepare.setNeighborUpdates(20);
        prepare.setContractedNodes(100);

        prepare.doWork();

        if (DebugUtility.isDebug()) {
            for (int i = 0; i < lg.getNodes(); i++)
                System.out.println("nodeId " + i + " level: " + lg.getLevel(i));
            AllCHEdgesIterator iter = lg.getAllEdges();
            while (iter.next()) {
                System.out.print(iter.getBaseNode() + " -> " + iter.getAdjNode() + " via edge " + iter.getEdge());
                if (iter.isShortcut())
                    System.out.print(" (shortcut)");
                System.out.println(" [weight: " + (new PreparationWeighting(weighting)).calcWeight(iter, false, -1) +"]");
            }
        }

        return lg;
    }

    @Test
    public void testOneToManyAllEdgesInCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        restrictedEdges.add(2);
        restrictedEdges.add(3);
        restrictedEdges.add(4);
        restrictedEdges.add(5);
        restrictedEdges.add(6);
        restrictedEdges.add(7);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(10);
        restrictedEdges.add(11);
        restrictedEdges.add(12);
        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    public void testManyToManyAllEdgesInCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        Weighting shortestWeighting = new ShortestWeighting(carEncoder);
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        restrictedEdges.add(2);
        restrictedEdges.add(3);
        restrictedEdges.add(4);
        restrictedEdges.add(5);
        restrictedEdges.add(6);
        restrictedEdges.add(7);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(10);
        restrictedEdges.add(11);
        restrictedEdges.add(12);
        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 1, null);
        sources.setData(1, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    /**
     * All start and goal nodes are in core
     */
    public void testOneToManySomeEdgesInCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    /**
     * All start and goal nodes are in core
     */
    public void testManyToManySomeEdgesInCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(1);
        restrictedEdges.add(5);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);
        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 1, null);
        sources.setData(1, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    /**
     * Not all start and goal nodes are in core
     */
    public void testOneToManySomeNodesInCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(5);
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        sources.setData(0, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    /**
     * Not all start and goal nodes are in core
     */
    public void testManyToManySomeNodesInCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 4, null);
        destinations.setData(1, 5, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(6.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    /**
     * Not all start and goal nodes are in core
     */
    public void testOneToManyNoNodesInCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    /**
     * There is no core
     */
    public void testOneToManyNoCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    /**
     * The connection is found already outside the core
     */
    public void testConnectionOutsideCoreFromHighestNode() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        restrictedEdges.add(8);
        sources.setData(0, 0, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    /**
     * The connection is found already outside the core
     */
    public void testConnectionOutsideCoreFromLowestNode() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        restrictedEdges.add(11);
        sources.setData(0, 5, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }

    @Test
    /**
     * The connection is found already outside the core
     */
    public void testConnectionOutsideCoreManyToMany() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        restrictedEdges.add(9);
        sources.setData(0, 5, null);
        sources.setData(1, 8, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 3, null);
        destinations.setData(1, 0, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(3.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(5.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(4.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    /**
     * There is no core
     */
    public void testAllToAll() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(8);
        restrictedEdges.add(9);
        restrictedEdges.add(11);
        restrictedEdges.add(12);

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(9);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        sources.setData(2, 2, null);
        sources.setData(3, 3, null);
        sources.setData(4, 4, null);
        sources.setData(5, 5, null);
        sources.setData(6, 6, null);
        sources.setData(7, 7, null);
        sources.setData(8, 8, null);
        MatrixLocations destinations = new MatrixLocations(9);
        destinations.setData(0, 0, null);
        destinations.setData(1, 1, null);
        destinations.setData(2, 2, null);
        destinations.setData(3, 3, null);
        destinations.setData(4, 4, null);
        destinations.setData(5, 5, null);
        destinations.setData(6, 6, null);
        destinations.setData(7, 7, null);
        destinations.setData(8, 8, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        float[] expected = new float[] {
                0.0f, 1.0f, 1.0f, 3.0f, 5.0f, 5.0f, 6.0f, 4.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 3.0f, 5.0f, 6.0f, 6.0f, 5.0f, 2.0f,
                1.0f, 1.0f, 0.0f, 2.0f, 4.0f, 5.0f, 5.0f, 5.0f, 2.0f,
                3.0f, 3.0f, 2.0f, 0.0f, 2.0f, 3.0f, 3.0f, 4.0f, 4.0f,
                5.0f, 5.0f, 4.0f, 2.0f, 0.0f, 1.0f, 1.0f, 2.0f, 5.0f,
                5.0f, 6.0f, 5.0f, 3.0f, 1.0f, 0.0f, 2.0f, 1.0f, 4.0f,
                6.0f, 6.0f, 5.0f, 3.0f, 1.0f, 2.0f, 0.0f, 2.0f, 5.0f,
                4.0f, 5.0f, 5.0f, 4.0f, 2.0f, 1.0f, 2.0f, 0.0f, 3.0f,
                1.0f, 2.0f, 2.0f, 4.0f, 5.0f, 4.0f, 5.0f, 3.0f, 0.0f
        };
        assertArrayEquals(expected, result.getTable(MatrixMetricsType.DISTANCE), 0);
    }

    @Test
    /**
     * The connection is found already outside the core
     */
    public void testStartAndTargetSameNodeOutsideCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        restrictedEdges.add(9);
        sources.setData(0, 1, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 1, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
    }

    @Test
    /**
     * The connection is found already outside the core
     */
    public void testStartAndTargetManyToManySameNodeOutsideCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        restrictedEdges.add(9);
        sources.setData(0, 1, null);
        sources.setData(0, 2, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 1, null);
        destinations.setData(0, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    /**
     * The connection is found already outside the core
     */
    public void testStartAndTargetSameNodeInsideCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(1);
        restrictedEdges.add(3);
        sources.setData(0, 1, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 1, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
    }

    @Test
    public void testStartAndTargetManyToManySameNodeInsideCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        restrictedEdges.add(3);
        sources.setData(0, 1, null);
        sources.setData(0, 2, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 1, null);
        destinations.setData(0, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    public void testStartAndTargetManyToManySameNodeAllNodesInsideCore() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createMediumGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        restrictedEdges.add(1);
        sources.setData(0, 1, null);
        sources.setData(0, 2, null);
        MatrixLocations destinations = new MatrixLocations(2);
        destinations.setData(0, 1, null);
        destinations.setData(0, 2, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, new PreparationWeighting(weighting), new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
        assertEquals(1.0, result.getTable(MatrixMetricsType.DISTANCE)[2], 0);
        assertEquals(0.0, result.getTable(MatrixMetricsType.DISTANCE)[3], 0);
    }

    @Test
    /**
     * All start and goal nodes are in core
     */
    public void testStoppingCriterion() {
        GraphHopperStorage graphHopperStorage = ToyGraphCreationUtil.createDiamondGraph(createGHStorage());
        CoreMatrixAlgorithm algorithm = new CoreMatrixAlgorithm();

        CoreTestEdgeFilter restrictedEdges = new CoreTestEdgeFilter();
        restrictedEdges.add(0);
        restrictedEdges.add(1);
        restrictedEdges.add(2);
        restrictedEdges.add(3);
        restrictedEdges.add(4);
        restrictedEdges.add(5);

        CHGraph g = contractGraph(graphHopperStorage, restrictedEdges);
        MatrixLocations sources = new MatrixLocations(2);
        sources.setData(0, 0, null);
        sources.setData(1, 1, null);
        MatrixLocations destinations = new MatrixLocations(1);
        destinations.setData(0, 4, null);

        MatrixRequest matrixRequest = new MatrixRequest();
        matrixRequest.setMetrics(MatrixMetricsType.DISTANCE);


        algorithm.init(matrixRequest, g, carEncoder, weighting, new CoreTestEdgeFilter());
        MatrixResult result = null;
        try{
            result = algorithm.compute(sources, destinations, MatrixMetricsType.DISTANCE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(2.0, result.getTable(MatrixMetricsType.DISTANCE)[0], 0);
        assertEquals(4.0, result.getTable(MatrixMetricsType.DISTANCE)[1], 0);
    }
}
