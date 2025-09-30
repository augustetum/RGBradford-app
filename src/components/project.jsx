import React, { useState, useEffect } from "react";
import { ChartContainer, ScatterPlot, LinePlot, ChartsXAxis, ChartsYAxis } from "@mui/x-charts";
import { AnimatePresence, motion } from 'framer-motion';

function Project({project}) {
    const [standardCurveData, setStandardCurveData] = useState(null);
    const [csvData, setCsvData] = useState([]);
    const [plateLayoutId, setPlateLayoutId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const getPlateIdByProject = async (projectId) => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`https://rgbradford-app.onrender.com/api/plate-layouts/by-project/${projectId}`, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch plate ID: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();
            setPlateLayoutId(data.plateLayoutId);
            return data.plateLayoutId;
        } catch (error) {
            console.error('Get plate ID error:', error);
            return null;
        }
    };

    const downloadExcel = async () => {
        if (!plateLayoutId) return;

        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`https://rgbradford-app.onrender.com/api/plate-analysis/${plateLayoutId}/xlsx`, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to download Excel file: ${response.status} ${response.statusText}`);
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `plate-analysis-${project.name}.xlsx`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (error) {
            console.error('Download Excel error:', error);
        }
    };

    const getPlateAnalysisCsv = async (plateAnalysisId) => {
        try {
            const token = localStorage.getItem("token");
            console.log("trying to get CSV")
            const response = await fetch(`https://rgbradford-app.onrender.com/api/plate-analysis/${plateAnalysisId}/csv`, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch CSV data: ${response.status} ${response.statusText}`);
            }

            const csvText = await response.text();

            // Parse CSV - find the "Well Analysis" section
            const lines = csvText.trim().split('\n');
            const wellAnalysisIndex = lines.findIndex(line => line.includes('Well Analysis'));

            if (wellAnalysisIndex !== -1) {
                // Start parsing from the line after "Well Analysis" header (skip header row)
                const dataLines = lines.slice(wellAnalysisIndex + 2);
                const parsedData = dataLines
                    .filter(line => line.trim().length > 0) // Filter out empty lines
                    .map(line => {
                        const values = line.split(',');
                        return {
                            row: parseInt(values[0]),
                            column: parseInt(values[1]),
                            blueGreenRatio: parseFloat(values[2]),
                            calculatedConcentration: parseFloat(values[3])
                        };
                    });
                setCsvData(parsedData);
            }
        } catch (error) {
            console.error('Get plate analysis CSV error:', error);
        }
    };

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
            console.log("got curve data")
            setError(null);

            // Get plate ID first, then call the CSV function
            const plateId = await getPlateIdByProject(project.id);
            if (plateId) {
                getPlateAnalysisCsv(plateId);
            }
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
            <main className='text-base'>
                <div className="text-center py-8">Loading standard curve data...</div>
            </main>
        );
    }

    if (error) {
        return (
            <main className='text-base'>
                <div className="text-center py-8 text-red-500">Error: {error}</div>
            </main>
        );
    }

    if (!standardCurveData) {
        return (
            <main className='text-base'>
                <div className="text-center py-8">No standard curve data available</div>
            </main>
        );
    }

    const scatterData = standardCurveData.points || [];
    const lineData = standardCurveData.regression || {};
    const scatterPoints = scatterData.map((obj) => ({
        x: obj.blueToGreenRatio,
        y: obj.concentration
    }));

    function equationToPoint(point, lineData) {
        return point.blueToGreenRatio * lineData.slope + lineData.intercept;
    }

    const lineY = scatterData.length > 0 ?
        [equationToPoint(scatterData[0], lineData), equationToPoint(scatterData[scatterData.length-1], lineData)] :
        [];

    const alphabet = Object.fromEntries(
        Array.from({ length: 26 }, (_, i) => [i, String.fromCharCode(65 + i)])
    );
    const range = (start, end) => {
        if (start > end) return [];
        return [...Array(end - start + 1)].map((_, i) => start + i);
    };

    const maxRow = csvData.length > 0 ? Math.max(...csvData.map((c) => c.row)) : 0;
    const maxColumn = csvData.length > 0 ? Math.max(...csvData.map((c) => c.column)) : 0;
    const minRow = csvData.length > 0 ? Math.min(...csvData.map((c) => c.row)) : 0;
    const minColumn = csvData.length > 0 ? Math.min(...csvData.map((c) => c.column)) : 0;
    console.log(maxRow, minRow)
    return (
        <main className='overflow-visible text-base'>
        <h1 className='text-3xl font-semibold'>{project.name}</h1>
        <h3 className='opacity-80 py-4'>{project.createdAt}</h3>
        <p className=" !text-white">{project.description}</p>

        <div className=" pt-2 bg-igem-white rounded-xl w-[min(90vw,50rem)] h-[30vh]  mx-auto my-4">
            <h2 className="text-igem-black text-base">Calibration curve, R<sup>2</sup>: {lineData.rsquared.toFixed(3) || 'N/A'} </h2>
            <ChartContainer
            margin={10}
            className="px-2 bg-igem-white rounded-xl"
            series={[
                { data: lineY, type: 'line'},
                { data: scatterPoints, type: 'scatter', markerSize: 8},
            ]}
            xAxis={[
            {
              // data: scatterData.map((x) => (x.blueToGreenRatio)),
              data: [scatterData[0].blueToGreenRatio, scatterData[scatterData.length-1].blueToGreenRatio],
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
        <button
                onClick={downloadExcel}
                disabled={!plateLayoutId}
                className="mt-8 px-6 py-2 bg-green-500 hover:bg-green-600 text-white font-bold rounded-lg disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors text-base"
            >
                Download Excel
        </button>
        {csvData.length > 0 && (
            <div className="w-full mt-4 mb-8 text-base">
                <h2 className="text-2xl font-semibold mb-4 text-center">Plate Analysis Results</h2>
                <div className="overflow-x-auto w-full">
                    <div className={`grid gap-2 mx-auto w-fit`}
                    style={{
                        gridTemplateColumns: `3rem repeat(${maxColumn - minColumn + 1}, minmax(4rem, 1fr))`,
                        gridTemplateRows: `3rem repeat(${maxRow - minRow + 1}, minmax(3rem, 1fr))`,
                    }}>
                        {csvData.map((well, i) => (
                        <div key={i} className={`bg-white text-black p-3 rounded-lg shadow-md text-center font-semibold flex items-center justify-center text-base`}
                        style={{
                            gridColumn: `${well.column - minColumn + 2} / ${well.column - minColumn + 3}`,
                            gridRow: `${well.row - minRow + 2} / ${well.row - minRow + 3}`,
                        }}>
                            <div className="text-sm">{well.calculatedConcentration.toFixed(4)}</div>
                        </div>
                    ))}

                    <div className="bg-igem-gray border-2 border-black rounded-xl text-base"
                    style={{
                        gridColumn: `2 / ${maxColumn - minColumn + 3}`,
                        gridRow: `1 / 2`,
                    }}></div>

                    {range(minColumn, maxColumn).map((val) => (
                        <p
                        key={val}
                        className="text-lg font-bold flex items-center justify-center text-base"
                        style={{
                            gridColumn: `${val - minColumn + 2} / ${val - minColumn + 3}`,
                            gridRow: `1 / 2`,
                        }}>
                            {val + 1}
                        </p>
                    ))}

                    <div className="bg-igem-gray border-2 border-black rounded-xl text-base"
                    style={{
                        gridRow: `2 / ${maxRow - minRow + 3}`,
                        gridColumn: `1 / 2`,
                    }}></div>

                    {range(minRow, maxRow).map((val) => (
                        <p
                        key={val}
                        className="text-lg font-bold flex items-center justify-center text-base"
                        style={{
                            gridRow: `${val - minRow + 2} / ${val - minRow + 3}`,
                            gridColumn: `1 / 2`,
                        }}>
                            {alphabet[val]}
                        </p>
                        ))}
                    </div>
                </div>
            </div>
        )}
        </main>
    )
}

export default Project