package usu.pajak.util;

public class Tax
{
    private Double getPercentage(Integer level)
    {
        Double percentage = 0.05;

        if(level == 2)
        {
            percentage = 0.15;
        }

        else if(level == 3)
        {
            percentage = 0.25;
        }

        else if(level >= 4)
        {
            percentage = 0.30;
        }

        return percentage;
    }

    private Double getUpperThreshold(Integer level)
    {
        Double threshold = 4166666.67;

        if(level == 2)
        {
            threshold = 20833333.33;
        }

        else if(level >= 3)
        {
            threshold = 41666666.67;
        }

        return threshold;
    }

    private Integer getTaxLevel(Double number)
    {
        Integer level = 1;

        if(number > 4166666.67 && number <= 20833333.33)
        {
            level = 2;
        }

        else if(number > 20833333.33 && number <= 41666666.67)
        {
            level = 3;
        }

        else if(number > 41666666.67)
        {
            level = 4;
        }

        return level;
    }

    public void calculate(Double[] numbers)
    {
        Integer level, levelCounter;

        Double number, threshold, remainder,
               multiplier, percentage, total = 0.0;

        for(int i = 0; i < numbers.length; i++)
        {
            number = numbers[i];
            levelCounter = 1;

            total = 0.0;
            remainder = 0.0;

            while(levelCounter <= 4)
            {
                threshold = getUpperThreshold(levelCounter);

                if(remainder == 0.0)
                {
                    remainder = number - threshold;
                }

                else
                {
                    remainder = remainder - threshold;
                }

                multiplier = remainder;
                level = getTaxLevel(multiplier);
                percentage = getPercentage(levelCounter);

                if(remainder > threshold)
                {
                    multiplier = threshold;
                }

                total += (percentage * multiplier);
                ++levelCounter;

                if(level == levelCounter)
                {
                    total += (remainder * getPercentage(levelCounter));
                    break;
                }
            }
        }

        System.out.println(total);
    }

    public static void main(String[] args)
    {
        Tax tax = new Tax();
        Double[] numbers = {50000000.00};
        tax.calculate(numbers);
    }
}