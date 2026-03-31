# NACH Reprocessing System - Frontend

## 🎯 Overview

React-based frontend application for managing and reprocessing NACH (National Automated Clearing House) transactions.

## ✨ Features

- 📁 **File Upload** with drag & drop support
- 📊 **Real-time Dashboard** with transaction statistics
- 🔍 **Advanced Search** and filtering capabilities
- 📋 **Interactive Transaction Table** with sorting
- 🔄 **Bulk Reprocessing** functionality
- 📱 **Responsive Design** for all devices
- 🎨 **Modern UI** with Tailwind CSS
- 📥 **Export to CSV** functionality
- 📋 **Copy to Clipboard** for easy data sharing

## 🛠️ Technology Stack

- **React 18** - Frontend framework
- **Tailwind CSS** - Styling framework
- **Lucide React** - Icon library
- **Axios** - HTTP client for API calls

## 🚀 Quick Start

### Prerequisites

- Node.js 16+ installed
- npm or yarn package manager

### Installation

```bash
# Navigate to your project frontend folder
cd C:\nach-reprocessing\frontend

# Initialize React app (if not done)
npx create-react-app .

# Install dependencies
npm install lucide-react axios

# Start development server
npm start
```

### Setup Files

1. Replace the default files with the provided components
2. Create the folder structure as shown below
3. Copy each file content to the corresponding location

## 📁 Project Structure

```
frontend/
├── public/
│   ├── index.html              # Main HTML template
│   └── favicon.ico
├── src/
│   ├── components/
│   │   ├── Dashboard.jsx       # Statistics dashboard
│   │   ├── FileUpload.jsx      # File upload component
│   │   ├── SearchFilter.jsx    # Search and filtering
│   │   └── TransactionTable.jsx # Transaction listing
│   ├── services/
│   │   └── api.js              # API communication & mock data
│   ├── utils/
│   │   └── helpers.js          # Utility functions
│   ├── styles/
│   │   └── index.css           # Global styles & Tailwind
│   ├── App.js                  # Main application component
│   └── index.js                # Entry point
├── package.json                # Dependencies and scripts
└── README.md                   # This file
```

## 🎯 Component Overview

### **Dashboard Component**

- **Total transaction count** and amounts
- **Success rate calculation**
- **Status breakdown** (Success, Error, Stuck, Failed, Reprocessed)
- **Visual statistics cards** with icons
- **Summary metrics** in gradient card

### **FileUpload Component**

- **Drag & drop interface** for easy file uploads
- **File validation** (.txt only, max 10MB)
- **Upload progress indication**
- **File guidelines** and format specifications
- **Error handling** for invalid files

### **SearchFilter Component**

- **Real-time search** across multiple fields
- **Status filtering** (All, Success, Error, Stuck, Failed, Reprocessed)
- **File type filtering** (All, DR, CR)
- **Date range filtering** with from/to dates
- **Quick filter buttons** for common searches
- **Active filter display** with remove options
- **Debounced search** for performance

### **TransactionTable Component**

- **Sortable columns** with visual indicators
- **Bulk selection** with select all/none
- **Copy to clipboard** for transaction details
- **Export to CSV** functionality
- **Status badges** with color coding
- **Error details** display
- **Action buttons** per row
- **Responsive design** for mobile devices

## 🔧 Development Features

### **Mock Data Mode**

- **Toggle switch** in header to enable/disable mock data
- **Sample transactions** with different statuses
- **Simulated API responses** for development
- **File upload simulation** with realistic delays
- **Reprocessing simulation** with status updates

### **Error Handling**

- **Graceful error messages** for users
- **Fallback to mock data** when API fails
- **Validation messages** for file uploads
- **Loading states** and progress indicators

### **Performance Optimizations**

- **Debounced search** (300ms delay)
- **Efficient filtering** and sorting algorithms
- **Optimized re-renders** with React hooks
- **Memory-efficient** state management

## 🎨 UI/UX Features

### **Modern Design**

- **Clean, professional interface**
- **Consistent color scheme** (blue, green, red, yellow)
- **Tailwind CSS** utility classes
- **Custom component styles**
- **Hover effects** and transitions

### **Responsive Layout**

- **Mobile-first** design approach
- **Flexible grid** layouts
- **Responsive tables** with horizontal scroll
- **Touch-friendly** buttons and interactions

### **Accessibility**

- **Semantic HTML** structure
- **Keyboard navigation** support
- **Screen reader** friendly
- **Color contrast** compliance
- **Focus indicators** for interactive elements

## 📊 Data Management

### **State Management**

- **React hooks** (useState, useEffect)
- **Centralized state** in App component
- **Prop drilling** for component communication
- **Local state** for component-specific data

### **Data Flow**

```
API/Mock Data → App State → Filtered Data → Components
```

### **Filtering & Sorting**

- **Multi-criteria filtering** (status, type, date, search)
- **Real-time filter application**
- **Sortable by any column**
- **Persistent sort state**

## 🔌 API Integration

### **Endpoints**

- `GET /api/nach/transactions` - Fetch all transactions
- `POST /api/nach/upload` - Upload NACH file
- `POST /api/nach/reprocess` - Reprocess selected transactions

### **Mock Data**

- **5 sample transactions** with different statuses
- **Realistic data** following NACH format
- **Error simulation** for development testing

### **Error Handling**

- **Request/Response interceptors**
- **Automatic retry** logic
- **Fallback to mock data**
- **User-friendly error messages**

## 🚀 Available Scripts

### **Development**

```bash
npm start          # Start development server (http://localhost:3000)
npm test           # Run test suite
npm run build      # Build for production
npm run eject      # Eject from Create React App
```

### **Production Build**

```bash
npm run build      # Creates optimized production build
```

## 🔧 Configuration

### **Environment Variables**

- `NODE_ENV` - Environment (development/production)
- API base URL automatically configured based on environment

### **Proxy Configuration**

- Development proxy to `http://localhost:8080` (backend server)
- Automatic API route forwarding

## 📱 Browser Support

- **Chrome** (recommended)
- **Firefox**
- **Safari**
- **Edge**
- **Mobile browsers** (iOS Safari, Chrome Mobile)

## 🔍 Troubleshooting

### **Common Issues**

**File upload not working:**

- Check file format (.txt only)
- Verify file size (< 10MB)
- Ensure mock data mode is enabled for testing

**API calls failing:**

- Enable mock data mode for frontend testing
- Check backend server status
- Verify API endpoints are correct

**Styling issues:**

- Ensure Tailwind CSS is loaded via CDN
- Check for CSS conflicts
- Verify component class names

**Performance issues:**

- Check for memory leaks in useEffect
- Optimize filter/sort functions
- Reduce unnecessary re-renders

## 🎯 Development Tips

1. **Start with mock data** enabled for frontend development
2. **Use browser DevTools** for debugging
3. **Test responsive design** on different screen sizes
4. **Validate all user inputs** before processing
5. **Handle loading states** for better UX

## 🚀 Deployment Options

### **Static Hosting**

- **Netlify** - Easy deployment with GitHub integration
- **Vercel** - Optimized for React applications
- **GitHub Pages** - Free hosting for static sites

### **Web Servers**

- **Apache** - Traditional web server
- **Nginx** - High-performance web server
- **Express.js** - Node.js server integration

### **Integration with Backend**

- Build files can be served from Java web server
- Maven can copy build files to webapp folder
- Single deployment for full-stack application

## 🤝 Contributing

1. Follow React best practices
2. Use meaningful component and function names
3. Add proper error handling
4. Test on multiple browsers
5. Maintain responsive design principles
6. Document complex logic with comments

## 📄 License

This project is part of the NACH Reprocessing System and follows the same licensing terms.

## 📞 Support

For technical support or questions about the frontend:

1. Check this README for common solutions
2. Review component code for implementation details
3. Test with mock data to isolate issues
4. Check browser console for error messages
"# nach-reprocessing" 
