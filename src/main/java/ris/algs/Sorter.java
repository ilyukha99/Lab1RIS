package ris.algs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Sorter {
    public static void main(String[] args) {
        int size = 10_000_000;
        System.out.println("length = " + size);

        int[] arr = genRandomArray(size);

        long start = System.nanoTime();
        Sorter.mergeSort(arr);
        long end = System.nanoTime();
        System.out.println("mergeSort: " +
                (end - start) / 1_000_000_000d + " sec");

        arr = genRandomArray(size);

        start = System.nanoTime();
        Sorter.qSort(arr,0, arr.length - 1);
        end = System.nanoTime();

        System.out.println("qSort: " +
                (end - start) / 1_000_000_000d + " sec");

//        arr = genRandomArray(size);
//
//        start = System.nanoTime();
//        Sorter.bubbleSort(arr);
//        end = System.nanoTime();
//
//        System.out.println("bubbleSort: " +
//                (end - start) / 1_000_000_000d + " sec\n");
    }

    public static int[] genRandomArray(int size) {
        ArrayList<Integer> list = new ArrayList<>(size);
        for (int it = 0; it < size; ++it) {
            list.add(it);
        }
        Collections.shuffle(list);

        int[] arr = new int[size];
        for (int it = 0; it < size; ++it) {
            arr[it] = list.get(it);
        }

        return arr;
    }

    public static int[] qSort(int[] nums, int left, int right) {
        if (nums.length < 2) {
            return nums;
        }

        if (left >= right) {
            return nums;
        }

        int medium = left + (right - left) / 2;
        int l = left;
        int r = right;
        int mediumVal = nums[medium];

        while (l <= r) {
            while (nums[l] < mediumVal) {
                ++l;
            }

            while (nums[r] > mediumVal) {
                --r;
            }

            if (l <= r) {
                int tmp = nums[l];
                nums[l] = nums[r];
                nums[r] = tmp;
                ++l;
                --r;
            }
        }

        if (left < r) {
            qSort(nums, left, r);
        }

        if (right > l) {
            qSort(nums, l, right);
        }

        return nums;
    }

    public static int[] mergeSort(int[] arr) {
        if (arr.length < 2) {
            return arr;
        }
        int medium = arr.length >> 1;

        int[] arr1 = Arrays.copyOfRange(arr, 0, medium);
        int[] arr2 = Arrays.copyOfRange(arr, medium, arr.length);
        return merge(mergeSort(arr1), mergeSort(arr2));
    }

    public static int[] merge(int[] arr1, int[] arr2) {
        int length = arr1.length + arr2.length;
        int[] res = new int[length];

        for (int i = 0, j = 0, it = 0; it < length; ++it) {
            if (i == arr1.length) {
                res[it] = arr2[j++];
            } else if (j == arr2.length) {
                res[it] = arr1[i++];
            } else {
                res[it] = (arr1[i] < arr2[j]) ? arr1[i++] : arr2[j++];
            }
        }

        return res;
    }

    public static int[] bubbleSort(int[] arr) {
        for (int i = 0; i < arr.length; ++i) {
            for (int j = 0; j < arr.length - i - 1; ++j) {
                if (arr[j] > arr[j + 1]) {
                    int tmp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = tmp;
                }
            }
        }
        return arr;
    }
}
