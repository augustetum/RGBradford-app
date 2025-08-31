import React from "react";
import plusIcon from '../assets/plus.svg';
import { ChartContainer, ScatterPlot, LinePlot, ChartsXAxis, ChartsYAxis } from "@mui/x-charts";

function Project({projects, currentProject}) {
    const curveData = projects[currentProject].curve
    const scatterPoints = curveData.pointsX.map((x, index) => ({
    x: x,
    y: curveData.pointsY[index]
    }));
    const linePoints = curveData.lineX.map((x, index) => ({
    x: x,
    y: curveData.lineY[index]
    }));
    return (
        <main className=''>
        <h1 className='text-3xl font-semibold'>{projects[currentProject].projectTitle}</h1>
        <h3 className='opacity-80 py-4'>{projects[currentProject].creationDate}</h3>
        {/* <button className='cursor-pointer w-[min(80vw,50rem)] h-[30vh] bg-igem-gray 
        rounded-xl flex justify-center items-center'>
            <div>
              <img src={plusIcon} className='mx-auto w-10 ' />
              <p className=''>upload plate</p>
            </div>
        </button> */}
        {/* <LineChart


        /> */}
        
        <div className=" pt-2 bg-igem-white rounded-xl w-[min(80vw,50rem)] h-[30vh]  mx-auto my-4">
            <h2 className="text-igem-black">Calibration curve, R<sup>2</sup>: {curveData.lineR2} </h2>
            <ChartContainer
            margin={10}
            className="px-2 bg-igem-white rounded-xl"
            series={[
                { data: curveData.lineY, type: 'line'},
                { data: scatterPoints, type: 'scatter', markerSize: 8},
            ]}
            xAxis={[
            {
              data: curveData.lineX,
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