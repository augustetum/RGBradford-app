import React from "react";
import plusIcon from '../assets/plus.svg';
import { ChartContainer, ScatterPlot, LinePlot, ChartsXAxis, ChartsYAxis } from "@mui/x-charts";
import data from "../data";
//function Project({projects, currentProject}) {
function Project() {
    
    const scatterData = data.projects[0].points;
    const lineData = data.projects[0].regression;
    const scatterPoints = scatterData.map((obj, index) => ({
    x: obj.blueToGreenRatio,
    y: obj.concentration
    }));
    function equationToPoint(point, lineData) {
      return point.blueToGreenRatio*lineData.slope + lineData.intercept;
    }
    const lineY = [equationToPoint(scatterData[0], lineData),equationToPoint(scatterData[scatterData.length-1], lineData)]

    return (
        <main className=''>
        {/* <h1 className='text-3xl font-semibold'>{projects[currentProject].name}</h1> */}
        {/* <h3 className='opacity-80 py-4'>{projects[currentProject].createdAt}</h3> */}
        <div className=" pt-2 bg-igem-white rounded-xl w-[min(90vw,50rem)] h-[30vh]  mx-auto my-4">
            <h2 className="text-igem-black">Calibration curve, R<sup>2</sup>: {lineData.rsquared} </h2>
            <ChartContainer
            margin={10}
            className="px-2 bg-igem-white rounded-xl"
            series={[
                { data: lineY, type: 'line'},
                { data: scatterPoints, type: 'scatter', markerSize: 8},
            ]}
            xAxis={[
            {
              data: scatterData.map((x) => (x.blueToGreenRatio)),
              scaleType: 'linear',
              height: 45,
            },
          ]}
            >
                <LinePlot />
                <ScatterPlot />
                <ChartsYAxis label="Concentration" />
                <ChartsXAxis label="Absorbance" />
            </ChartContainer>
        </div>
        </main>
    )
}

export default Project