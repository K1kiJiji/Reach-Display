package kikijiji.reachdisplay.config;


import java.util.List;
import java.util.ArrayList;


public class ReachDisplayConfig
{
    // 텍스트 레이어
    public boolean showReach      = true;
    public boolean showShadow     = true;
    public boolean showBackground = true;

    public int mainColor       = 0xFFFFFFFF;
    public int shadowColor     = 0xFF000000;
    public int backgroundColor = 0x50000000;

    // 크기
    public int scale = 100;

    // 위치
    public int offsetX = 0;
    public int offsetY = 0;


    // 엔티티 필터
    public boolean useWhitelist   = true;
    public List<String> whitelist = new ArrayList<>();
    public List<String> blacklist = new ArrayList<>();


    // 거리별 색상
    public boolean enableDistanceColor = true;
    public List<DistanceColorBand> distanceBands = new ArrayList<>();

    public static class DistanceColorBand
    {
        public double maxDistance;
        public int mainColor;
        public int shadowColor;
        public int backgroundColor;
    }

    public ReachDisplayConfig()
    {
        DistanceColorBand band1 = new DistanceColorBand();
        band1.maxDistance = 1.0;
        band1.mainColor = 0xFF00FF00;
        band1.shadowColor = 0xFF003300;
        band1.backgroundColor = 0x40003300;
        distanceBands.add(band1);

        DistanceColorBand band2 = new DistanceColorBand();
        band2.maxDistance = 2.0;
        band2.mainColor = 0xFFFFFF00;
        band2.shadowColor = 0xFF333300;
        band2.backgroundColor = 0x40FFFF00;
        distanceBands.add(band2);

        DistanceColorBand band3 = new DistanceColorBand();
        band3.maxDistance = 3.0;
        band3.mainColor = 0xFFFFA500;
        band3.shadowColor = 0xFF331A00;
        band3.backgroundColor = 0x40FFA500;
        distanceBands.add(band3);

        DistanceColorBand band4 = new DistanceColorBand();
        band4.maxDistance = 9999.0;
        band4.mainColor = 0xFFFF0000;
        band4.shadowColor = 0xFF330000;
        band4.backgroundColor = 0x40FF0000;
        distanceBands.add(band4);
    }


    // 표시 유지 방식
    public boolean keepLastDistance = true;
    public double resetAfterSeconds = 15.0;


    // 표시 포맷
    public enum DisplayMode
    {
        NUMBER_ONLY,
        WITH_BLOCKS,
        WITH_M
    }

    public DisplayMode displayMode = DisplayMode.WITH_BLOCKS;
}