/*
    int lorem = 6009;
    String ipsum = "dolor sit amet";

 */
class Winwin
{
    boolean findTruth(String[] everything)
    {
        var theTruth = true;

        var i = 0;
        for(; i < 2; i++)
        {
            theTruth = theTruth && everything[i];
        }

        return theTruth;
    }

    void main(String[] args)
    {
        var winner = new Winwin();

        return winner.findTruth(parameter);
    }
}