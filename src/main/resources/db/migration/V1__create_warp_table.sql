CREATE TABLE warp (
                       playeruuid UUID NOT NULL,
                       warpname VARCHAR(255) NOT NULL,
                       worldname VARCHAR(255) NOT NULL,
                       x DOUBLE PRECISION NOT NULL,
                       y DOUBLE PRECISION NOT NULL,
                       z DOUBLE PRECISION NOT NULL,
                       pitch NUMERIC(9,6) NOT NULL,
                       yaw NUMERIC(9,6) NOT NULL,
                       PRIMARY KEY (playeruuid, warpname)
);