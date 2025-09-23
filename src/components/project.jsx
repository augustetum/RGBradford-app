import React, { useState, useEffect } from "react";
import { ChartContainer, ScatterPlot, LinePlot, ChartsXAxis, ChartsYAxis } from "@mui/x-charts";
import { AnimatePresence, motion } from 'framer-motion';

function Project({project}) {
    console.log("proj is proj.jsx" + project)
    const [standardCurveData, setStandardCurveData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
  console.log(project);

    const getStandardCurveData = async () => {
      console.log("trying to get")  
      try {
            setLoading(true);
            const token = localStorage.getItem("token");
            const response = await fetch(`https://rgbradford-app.onrender.com/api/standard-curve/proj/${project.id}`, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch standard curve data: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();
            setStandardCurveData(data);
            console.log(data);
            setError(null);
        } catch (error) {
            console.error('Get standard curve data error:', error);
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (project?.id) {
            getStandardCurveData();
        }
    }, [project?.id]);

    if (loading) {
        return (
            <main className=''>
                <div className="text-center py-8">Loading standard curve data...</div>
            </main>
        );
    }

    if (error) {
        return (
            <main className=''>
                <div className="text-center py-8 text-red-500">Error: {error}</div>
            </main>
        );
    }

    if (!standardCurveData) {
        return (
            <main className=''>
                <div className="text-center py-8">No standard curve data available</div>
            </main>
        );
    }

    const scatterData = standardCurveData.points || [];
    const lineData = standardCurveData.regression || {};
    const scatterPoints = scatterData.map((obj) => ({
        y: obj.blueToGreenRatio,
        x: obj.concentration
    }));

    function equationToPoint(point, lineData) {
        return point.concentration * lineData.slope + lineData.intercept;
    }

    const lineY = scatterData.length > 0 ?
        [equationToPoint(scatterData[0], lineData), equationToPoint(scatterData[scatterData.length-1], lineData)] :
        [];

    return (
        <main className=''>
        <h1 className='text-3xl font-semibold'>{project.name}</h1>
        <h3 className='opacity-80 py-4'>{project.createdAt}</h3>
        <p>{project.description}</p>
        <div className=" pt-2 bg-igem-white rounded-xl w-[min(90vw,50rem)] h-[30vh]  mx-auto my-4">
            <h2 className="text-igem-black">Calibration curve, R<sup>2</sup>: {lineData.rsquared || 'N/A'} </h2>
            <ChartContainer
            margin={10}
            className="px-2 bg-igem-white rounded-xl"
            series={[
                { data: lineY, type: 'line'},
                { data: scatterPoints, type: 'scatter', markerSize: 8},
            ]}
            xAxis={[
            {
              data: scatterData.map((x) => (x.concentration)),
              scaleType: 'linear',
              height: 45,
            },
          ]}
            >
                <LinePlot />
                <ScatterPlot />
                <ChartsYAxis label="Absorbance" />
                <ChartsXAxis label="Concentration" />
            </ChartContainer>
        </div>
        </main>
    )
}

export default Project