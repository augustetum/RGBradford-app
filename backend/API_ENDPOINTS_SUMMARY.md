# Plate Analysis API Endpoints Summary

## 1. Plate Layout Management

### Core CRUD Operations
- `GET /api/plate-layouts` - List all plate layouts (with pagination)
- `GET /api/plate-layouts/{id}` - Get specific plate layout
- `POST /api/plate-layouts` - Create new plate layout
- `PUT /api/plate-layouts/{id}` - Update plate layout
- `DELETE /api/plate-layouts/{id}` - Delete plate layout

### Well Management within Plates
- `GET /api/plate-layouts/{id}/wells` - Get all wells for a plate
- `POST /api/plate-layouts/{id}/wells` - Add wells to plate layout
- `PUT /api/plate-layouts/{id}/wells` - Update wells in plate layout

## 2. Well Management

### Core CRUD Operations
- `GET /api/wells` - List all wells (with filters: plateLayoutId, wellType, replicateGroup)
- `GET /api/wells/{id}` - Get specific well
- `POST /api/wells` - Create new well
- `PUT /api/wells/{id}` - Update well
- `DELETE /api/wells/{id}` - Delete well

### Filtering Endpoints
- `GET /api/wells/plate/{plateLayoutId}` - Get wells by plate
- `GET /api/wells/type/{wellType}` - Get wells by type
- `GET /api/wells/replicate/{group}` - Get wells by replicate group

## 3. Plate Analysis (Enhanced)

### Core Analysis
- `POST /api/plate-analysis/analyze` - Analyze plate (existing)
- `GET /api/plate-analysis/{plateLayoutId}` - Get analysis results
- `GET /api/plate-analysis/{plateLayoutId}/summary` - Get analysis summary
- `DELETE /api/plate-analysis/{plateLayoutId}` - Delete analysis results
- `POST /api/plate-analysis/{plateLayoutId}/reanalyze` - Reanalyze with new params

### Data Export
- `GET /api/plate-analysis/{plateLayoutId}/csv` - Download CSV (existing)

## Request/Response DTOs

### Plate Layout DTOs
- `CreatePlateLayoutRequest` - For creating new plate layouts
- `UpdatePlateLayoutRequest` - For updating existing plate layouts
- `PlateLayoutResponse` - Response with plate layout details

### Well DTOs
- `WellRequest` - For creating/updating wells
- `WellResponse` - Response with well details

### Analysis DTOs
- `PlateAnalysisParams` - Analysis parameters (existing)
- `WellAnalysisResult` - Individual well analysis results
- `WellAnalysisCsvWriter` - CSV export functionality (existing)

## Key Features Implemented

### Pagination
- All list endpoints support Spring Data pagination
- Configurable page size and sorting

### Filtering
- Wells can be filtered by plate layout, type, and replicate group
- Plate layouts can be filtered by project

### Error Handling
- Proper HTTP status codes (200, 201, 204, 404)
- Consistent error responses

### Data Validation
- Input validation through DTOs
- Proper null checking and error handling

## Database Repository Methods Added

### WellRepository Extensions
- `Page<Well> findByPlateLayoutId(Long plateLayoutId, Pageable pageable)`
- `Page<Well> findByType(WellType type, Pageable pageable)`
- `Page<Well> findByReplicateGroup(String replicateGroup, Pageable pageable)`
- `void deleteByPlateLayoutId(Long plateLayoutId)`

## Usage Examples

### Creating a Plate Layout
```bash
POST /api/plate-layouts
{
  "rows": 8,
  "columns": 12,
  "project": {"id": 1}
}
```

### Adding Wells to a Plate
```bash
POST /api/plate-layouts/1/wells
[
  {
    "row": 1,
    "column": 1,
    "type": "STANDARD",
    "standardConcentration": 0.5
  },
  {
    "row": 1,
    "column": 2,
    "type": "SAMPLE",
    "sampleName": "Sample A",
    "dilutionFactor": 1.0
  }
]
```

### Analyzing a Plate
```bash
POST /api/plate-analysis/analyze
Content-Type: multipart/form-data
- plateLayoutId: 1
- params: {"columns": 12, "rows": 8, "xOrigin": 100, "yOrigin": 100, "xEnd": 800, "yEnd": 600, "wellDiameter": 50}
- image: [image file]
```

### Getting Analysis Results
```bash
GET /api/plate-analysis/1
```

### Getting Analysis Summary
```bash
GET /api/plate-analysis/1/summary
```
