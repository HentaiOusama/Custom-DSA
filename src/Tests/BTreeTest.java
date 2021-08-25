import com.hentai_productions.Trees.BTree;

import java.util.Scanner;

public class BTreeTest {
    public static void main(String[] args) {
        BTree<Integer> bTree;
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter order of BTree : ");
        bTree = new BTree<>(scanner.nextInt());

        int count;
        System.out.print("Enter number of elements you want to insert : ");
        count = scanner.nextInt();

        System.out.print("Use random mode (true / false) : ");
        boolean randomMode = scanner.nextBoolean();

        for (int i = 1; i <= count; i++) {
            if (randomMode) {
                bTree.put((int) (Math.random() * 10000) + 1);
            } else {
                System.out.print("Enter element " + i + " : ");
                bTree.put(scanner.nextInt());
            }
        }

        if (bTree.isEmpty()) {
            System.out.println("The tree is empty....");
        } else {
            System.out.println(bTree);
        }
    }
}
