package org.example;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class Application {

    public int networkDelayTime(int[][] times, int n, int k) {
        List<List<int[]>> nodes = new ArrayList<>();

        for (int i = 0; i <= n; ++i) {
            nodes.add(new ArrayList<>());
        }

        for (int[] time : times) {

            int ui = time[0];
            int vi = time[1];
            int ti = time[2];

            nodes.get(ui).add(new int[] {vi, ti});
        }

        int[] markedNodes = new int[n+1];
        Arrays.fill(markedNodes, Integer.MAX_VALUE);

        process(nodes, k, 0, markedNodes);

        int res = 0;
        for (int i = 1; i <= n; ++i) {
            if (markedNodes[i] == Integer.MAX_VALUE) {
                return -1;
            }

            res = Math.max(res, markedNodes[i]);
        }

        return res;

    }

    private void process(List<List<int[]>> nodes, int node, int currTime, int[] markedNodes) {

        if (markedNodes[node] <= currTime) {
            return;
        }

        markedNodes[node] = currTime;

        for (int[] next : nodes.get(node)) {

            int nextNode = next[0];

            process(nodes, nextNode, currTime+next[1], markedNodes);
        }

    }

    public static void main(String[] args) {
        Application app = new Application();

        app.networkDelayTime(new int[][] { {1,2,1}, {2,3,1}, {1,4,4}, {3,4,1} }, 4, 1);


        // SpringApplication.run(Application.class, args);
    }
}

