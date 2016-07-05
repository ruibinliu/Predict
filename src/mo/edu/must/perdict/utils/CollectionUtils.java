
package mo.edu.must.perdict.utils;

import java.util.ArrayList;

public class CollectionUtils {
    /**
     * 冒泡法排序<br/> <li>比较相邻的元素。如果第一个比第二个大，就交换他们两个。</li> <li>
     * 对每一对相邻元素作同样的工作，从开始第一对到结尾的最后一对。在这一点，最后的元素应该会是最大的数。</li> <li>
     * 针对所有的元素重复以上的步骤，除了最后一个。</li> <li>持续每次对越来越少的元素重复上面的步骤，直到没有任何一对数字需要比较。</li>
     * 
     * @param numbers 需要排序的整型数组
     */
    public static void bubbleSort(double[] numbers) {
        double temp; // 记录临时中间值
        int size = numbers.length; // 数组大小
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (numbers[i] < numbers[j]) { // 交换两数的位置
                    temp = numbers[i];
                    numbers[i] = numbers[j];
                    numbers[j] = temp;
                }
            }
        }
    }

    /**
     * 快速排序<br/>
     * <ul>
     * <li>从数列中挑出一个元素，称为“基准”</li>
     * <li>重新排序数列，所有元素比基准值小的摆放在基准前面，所有元素比基准值大的摆在基准的后面（相同的数可以到任一边）。在这个分割之后，
     * 该基准是它的最后位置。这个称为分割（partition）操作。</li>
     * <li>递归地把小于基准值元素的子数列和大于基准值元素的子数列排序。</li>
     * </ul>
     * 
     * @param numbers
     * @param start
     * @param end
     */
    public static void quickSort(double[] numbers, int start, int end) {
        if (start < end) {
            double base = numbers[start]; // 选定的基准值（第一个数值作为基准值）
            double temp; // 记录临时中间值
            int i = start, j = end;
            do {
                while ((numbers[i] < base) && (i < end))
                    i++;
                while ((numbers[j] > base) && (j > start))
                    j--;
                if (i <= j) {
                    temp = numbers[i];
                    numbers[i] = numbers[j];
                    numbers[j] = temp;
                    i++;
                    j--;
                }
            } while (i <= j);
            if (start < j)
                quickSort(numbers, start, j);
            if (end > i)
                quickSort(numbers, i, end);
        }
    }
}
